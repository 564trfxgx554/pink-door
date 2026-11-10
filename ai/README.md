# Pink Door AI — Python + LangChain

粉色小门后的 AI 小助手“小婷段”。它是**独立的 Python 服务**，只暴露内部接口给 Java 调用，
前端不直接访问，也不需要知道它的存在。

## 技术栈

- FastAPI + Uvicorn
- LangChain（`langchain-core` + `langchain-openai` / `langchain-anthropic`）
- SSE 流式输出
- 默认 `mock` 模型：不需要 API key，先把链路跑通

## 启动

```bash
cd pink-door-ai
uv sync
uv run uvicorn app.main:app --host 127.0.0.1 --port 8000
```

也可以直接运行 `app/main.py`：

```bash
cd pink-door-ai
uv run python app/main.py
```

注意：一定要在 `pink-door-ai` 目录里执行。项目根目录也有自己的 `.venv` / `pyproject.toml`，
如果在根目录执行 `uv run`，会用到那个环境，里面没有 FastAPI / LangChain。

如果不用 uv：

```bash
cd pink-door-ai
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

健康检查：

```bash
curl http://127.0.0.1:8000/internal/health
```

## 接口

| 方法 | 路径 | 调用方 | 说明 |
| --- | --- | --- | --- |
| GET | `/internal/health` | 运维 | 健康检查 |
| POST | `/internal/chat/stream` | Java | SSE 流式聊天 |

请求示例：

```bash
curl -N -X POST http://127.0.0.1:8000/internal/chat/stream \
  -H "Content-Type: application/json" \
  -H "X-Internal-Token: change-me" \
  -d '{"user_id":1,"nickname":"dyt","messages":[{"role":"user","content":"今天有点难过"}],"facts":{}}'
```

响应是 SSE：

```text
data: {"type":"delta","content":"我"}

data: {"type":"delta","content":"在"}

data: {"type":"done"}
```

## 接入真实模型

在 `.env` 里改：

```env
# Claude
AI_PROVIDER=anthropic
AI_MODEL=claude-opus-4-8
AI_API_KEY=sk-ant-...

# 或 DeepSeek
AI_PROVIDER=openai
AI_MODEL=deepseek-chat
AI_BASE_URL=https://api.deepseek.com/v1
AI_API_KEY=sk-...
```

阿里云百炼 Qwen（OpenAI 兼容模式）：

```env
AI_PROVIDER=openai
AI_MODEL=qwen3.8-flash
AI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
AI_API_KEY=sk-...
```

改完重启服务即可，Java 和前端不用动。

## 目录

```text
app/
├── main.py           FastAPI 入口与内部令牌校验
├── chat_service.py   组装 prompt、模型流式转 SSE、心跳
├── llm.py            LangChain 模型工厂 + mock 模型
├── persona.py        小婷段的人设与边界（改性格只改这里）
├── schemas.py        请求结构
└── config.py         环境变量配置
```

## 说明

- Python 无状态：聊天历史由 Java 从 MySQL 读取后传过来。
- `X-Internal-Token` 是服务间共享密钥，不是用户 JWT。
- 不要把 8000 端口直接暴露到公网。
