import os
from typing import Any, Iterator, Optional

from langchain_core.callbacks import CallbackManagerForLLMRun
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import AIMessage, AIMessageChunk, BaseMessage
from langchain_core.outputs import ChatGeneration, ChatGenerationChunk, ChatResult

from app.config import settings


class MockChatModel(BaseChatModel):
    """本地 mock 模型：按关键词返回一段温柔回复，并逐字流式输出。

    用途是先把 Java -> Python -> 前端的流式管线跑通，
    不需要任何模型 API key。
    """

    @property
    def _llm_type(self) -> str:
        return "pink-door-mock"

    def _generate(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        run_manager: Optional[CallbackManagerForLLMRun] = None,
        **kwargs: Any,
    ) -> ChatResult:
        text = _build_mock_reply(messages)
        return ChatResult(generations=[ChatGeneration(message=AIMessage(content=text))])

    def _stream(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        run_manager: Optional[CallbackManagerForLLMRun] = None,
        **kwargs: Any,
    ) -> Iterator[ChatGenerationChunk]:
        text = _build_mock_reply(messages)
        for char in text:
            chunk = ChatGenerationChunk(message=AIMessageChunk(content=char))
            if run_manager:
                run_manager.on_llm_new_token(char, chunk=chunk)
            yield chunk


def _last_user_text(messages: list[BaseMessage]) -> str:
    for message in reversed(messages):
        if message.type == "human" and isinstance(message.content, str):
            return message.content.strip()
    return ""


def _build_mock_reply(messages: list[BaseMessage]) -> str:
    text = _last_user_text(messages)
    date_keywords = ("去哪", "约会", "周末", "玩什么", "地点", "出去")
    sad_keywords = ("难过", "累", "委屈", "烦", "哭", "压力", "不想", "难受")

    if any(keyword in text for keyword in date_keywords):
        return (
            "如果这周末想出门，我建议先选一个能慢慢逛、还能拍照的地方。"
            "你更想要安静一点，还是热闹一点？我帮你排一版时间线，顺便准备下雨的备选方案。"
        )
    if any(keyword in text for keyword in sad_keywords):
        return (
            "我在呢，先抱抱你。听起来今天真的挺不容易的。"
            "要不要先从最让你难受的那一件事说起？你想让我听着，还是一起想办法，都可以。"
        )
    short_text = text[:30] + ("…" if len(text) > 30 else "")
    return (
        f"我在认真听。你说“{short_text}”，"
        "我想先知道：你现在更想让我陪你聊聊，还是一起想个办法？"
    )


def build_chat_model() -> BaseChatModel:
    """按配置创建 LangChain 聊天模型。"""

    provider = settings.ai_provider.lower()

    if provider == "mock":
        return MockChatModel()

    if provider == "openai":
        from langchain_openai import ChatOpenAI

        if not settings.ai_api_key:
            raise RuntimeError("AI_PROVIDER=openai 时必须配置 AI_API_KEY")
        return ChatOpenAI(
            model=settings.ai_model,
            api_key=settings.ai_api_key,
            base_url=settings.ai_base_url or None,
            max_tokens=settings.ai_max_tokens,
            streaming=True,
        )

    if provider == "anthropic":
        from langchain_anthropic import ChatAnthropic

        if settings.ai_api_key:
            os.environ["ANTHROPIC_API_KEY"] = settings.ai_api_key
        return ChatAnthropic(
            model=settings.ai_model,
            max_tokens=settings.ai_max_tokens,
            streaming=True,
        )

    raise RuntimeError(f"不支持的 AI_PROVIDER: {settings.ai_provider}")
