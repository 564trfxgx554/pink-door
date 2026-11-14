// Pink Door — AI 聊天 API 层。
// 前端只访问 Java；Java 再转发到内部的 Python AI 服务。

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function authHeaders() {
  const token = sessionStorage.getItem('pink-door-token')
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  }
}

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: authHeaders()
  })

  if (response.status === 401) {
    sessionStorage.removeItem('pink-door-token')
    throw new Error('登录已过期，请重新登录。')
  }
  if (!response.ok) {
    let message = '请求失败，请稍后再试。'
    try {
      const data = await response.json()
      if (data?.message) message = data.message
    } catch {
      // 非 JSON 错误体，使用默认提示。
    }
    throw new Error(message)
  }
  return response.json()
}

export async function listConversations() {
  const result = await request('/api/ai/conversations')
  return result.data || []
}

export async function listMessages(conversationId) {
  const result = await request(`/api/ai/conversations/${conversationId}/messages`)
  return result.data || []
}

export async function deleteConversation(conversationId) {
  await request(`/api/ai/conversations/${conversationId}`, { method: 'DELETE' })
}

/**
 * 发起流式聊天。
 * onEvent 会收到 meta / delta / done / error 四种事件。
 */
export async function streamChat({ conversationId, content, onEvent, signal }) {
  const response = await fetch(`${API_BASE_URL}/api/ai/chat/stream`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ conversationId, content }),
    signal
  })

  if (response.status === 401) {
    sessionStorage.removeItem('pink-door-token')
    throw new Error('登录已过期，请重新登录。')
  }
  if (!response.ok || !response.body) {
    let message = '发送失败，请稍后再试。'
    try {
      const data = await response.json()
      if (data?.message) message = data.message
    } catch {
      // 非 JSON 错误体，使用默认提示。
    }
    throw new Error(message)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    buffer = buffer.replace(/\r\n/g, '\n')

    let index = buffer.indexOf('\n\n')
    while (index !== -1) {
      const rawEvent = buffer.slice(0, index)
      buffer = buffer.slice(index + 2)
      for (const line of rawEvent.split('\n')) {
        if (!line.startsWith('data:')) continue
        const data = line.slice(5).trim()
        if (!data) continue
        try {
          onEvent(JSON.parse(data))
        } catch (error) {
          console.warn('无法解析 AI 事件:', data, error)
        }
      }
      index = buffer.indexOf('\n\n')
    }
  }
}
