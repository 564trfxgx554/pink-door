from pydantic import BaseModel, Field


class ChatMessage(BaseModel):
    """一条历史消息，role 只有 user / assistant。"""

    role: str
    content: str


class ChatRequest(BaseModel):
    """Java 转发过来的聊天请求。Python 无状态，历史由 Java 提供。"""

    user_id: int
    nickname: str = ""
    messages: list[ChatMessage] = Field(min_length=1)
    facts: dict[str, str] = Field(default_factory=dict)
