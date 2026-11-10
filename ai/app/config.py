from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

# 配置文件路径固定到项目根目录，避免从不同工作目录启动时读不到 .env。
BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    """AI 服务的全部配置，均可用同名大写环境变量覆盖。"""

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # Java 调用 Python 时携带的内部令牌，不是用户 JWT。
    ai_internal_token: str = "change-me"

    # mock：本地流式演示，不需要 API key；
    # openai：任何 OpenAI 兼容接口（DeepSeek、Qwen 等）；
    # anthropic：Claude 官方接口。
    ai_provider: str = "mock"
    ai_model: str = "claude-opus-4-8"
    ai_api_key: str = ""
    ai_base_url: str | None = None
    ai_max_tokens: int = 800


settings = Settings()
