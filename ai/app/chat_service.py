import asyncio
import json
import logging
from collections.abc import AsyncIterator

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage

from app.llm import build_chat_model
from app.persona import build_system_prompt
from app.schemas import ChatRequest

logger = logging.getLogger(__name__)

HEARTBEAT_SECONDS = 15


def sse(payload: dict) -> str:
    """把事件编码成 SSE data 行。"""

    return f"data: {json.dumps(payload, ensure_ascii=False)}\n\n"


async def stream_chat(request: ChatRequest) -> AsyncIterator[str]:
    """组装 prompt，调用 LangChain 模型，并输出 SSE 事件。"""

    system_prompt = build_system_prompt(request.facts, request.nickname)
    messages = [SystemMessage(content=system_prompt)]

    for item in request.messages[-20:]:
        content = item.content.strip()
        if not content:
            continue
        if item.role == "assistant":
            messages.append(AIMessage(content=content))
        else:
            messages.append(HumanMessage(content=content))

    yield ": connected\n\n"

    try:
        model = build_chat_model()
        async for event in _with_heartbeat(_model_stream(model, messages)):
            yield event
        yield sse({"type": "done"})
    except Exception:
        logger.exception("AI 流式生成失败")
        yield sse({
            "type": "error",
            "message": "小助手暂时有点走神，等会儿再试试好吗？",
        })


async def _model_stream(model, messages) -> AsyncIterator[str]:
    """把 LangChain 的模型增量转换成 SSE。"""

    async for chunk in model.astream(messages):
        content = chunk.content
        if isinstance(content, str) and content:
            yield sse({"type": "delta", "content": content})
        elif isinstance(content, list):
            for part in content:
                if isinstance(part, dict) and part.get("type") == "text":
                    text = part.get("text", "")
                    if text:
                        yield sse({"type": "delta", "content": text})


async def _with_heartbeat(stream: AsyncIterator[str]) -> AsyncIterator[str]:
    """模型思考较久时发送 SSE 注释，避免代理提前断开。"""

    iterator = stream.__aiter__()
    while True:
        try:
            item = await asyncio.wait_for(iterator.__anext__(), timeout=HEARTBEAT_SECONDS)
        except asyncio.TimeoutError:
            yield ": ping\n\n"
            continue
        except StopAsyncIteration:
            break
        yield item
