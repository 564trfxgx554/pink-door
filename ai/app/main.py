import logging
import secrets
import sys
from pathlib import Path

# 直接运行 app/main.py 时，把项目根目录加入 sys.path，保证能 import app.*。
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from fastapi import FastAPI, Header, HTTPException
from fastapi.responses import StreamingResponse

from app.chat_service import stream_chat
from app.config import settings
from app.persona import PERSONA_NAME
from app.schemas import ChatRequest

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s - %(message)s",
)

app = FastAPI(title="Pink Door AI", version="0.1.0")


@app.get("/internal/health")
async def health() -> dict:
    return {
        "status": "ok",
        "provider": settings.ai_provider,
        "persona": PERSONA_NAME,
    }


@app.post("/internal/chat/stream")
async def chat_stream(
    request: ChatRequest,
    x_internal_token: str = Header(default="", alias="X-Internal-Token"),
) -> StreamingResponse:
    """仅允许 Java 通过内部令牌调用，不对外暴露。"""

    if not secrets.compare_digest(x_internal_token, settings.ai_internal_token):
        raise HTTPException(status_code=401, detail="invalid internal token")

    return StreamingResponse(
        stream_chat(request),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
        },
    )


if __name__ == "__main__":
    # 直接运行 app/main.py 时启动 uvicorn；否则脚本只会定义应用然后正常退出。
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
