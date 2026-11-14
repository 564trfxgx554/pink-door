// Pink Door — 默契问答 API 与 WebSocket 封装。
// 页面不直接写 onmessage，所有服务端消息统一交给 onMessage 分发。

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const WS_BASE_URL = API_BASE_URL.replace(/^http/, 'ws')

function authHeaders() {
  const token = sessionStorage.getItem('pink-door-token')
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  }
}

/**
 * 创建或复用当前情侣空间的房间。
 * 同一空间同时只有一个未结束房间，两个人会拿到同一个 roomId。
 */
export async function createRoom() {
  const response = await fetch(`${API_BASE_URL}/api/quiz/rooms`, {
    method: 'POST',
    headers: authHeaders()
  })

  if (response.status === 401) {
    sessionStorage.removeItem('pink-door-token')
    throw new Error('登录已过期，请重新登录。')
  }
  if (!response.ok) {
    let message = '创建房间失败，请稍后再试。'
    try {
      const data = await response.json()
      if (data?.message) message = data.message
    } catch {
      // 非 JSON 错误体，使用默认提示。
    }
    throw new Error(message)
  }
  const result = await response.json()
  return result.data
}

/**
 * 连接默契问答 WebSocket。
 *
 * @param {string} token   登录 token，浏览器的 WebSocket 不能带 Authorization 头，只能走查询参数
 * @param {number} roomId  房间 ID
 * @param {(data: object) => void} onMessage 服务端所有 JSON 消息
 * @param {(status: 'connected'|'closed'|'failed') => void} onStatus 连接状态
 */
export function connectQuiz({ token, roomId, onMessage, onStatus }) {
  let socket = null
  let retries = 0
  let closedByUser = false
  let reconnectTimer = null

  function send(payload) {
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(payload))
    }
  }

  function open() {
    const url = `${WS_BASE_URL}/ws/quiz?token=${encodeURIComponent(token)}`
    socket = new WebSocket(url)

    socket.onopen = () => {
      // 连上就清零，避免整局累计断线 5 次后永久不再重连。
      retries = 0
      onStatus?.('connected')
      // 每次连上都补一次 join，刷新或断线重连都能恢复现场。
      send({ type: 'join', roomId })
    }
    socket.onmessage = (event) => {
      let data
      try {
        data = JSON.parse(event.data)
      } catch {
        return
      }
      onMessage?.(data)
    }
    socket.onclose = () => {
      onStatus?.('closed')
      if (closedByUser) return
      if (retries >= 5) {
        onStatus?.('failed')
        return
      }
      retries += 1
      reconnectTimer = setTimeout(open, 2000)
    }
    socket.onerror = () => socket?.close()
  }

  open()

  return {
    sendReady() {
      send({ type: 'ready' })
    },
    sendAnswer(roundNo, optionIndex) {
      send({ type: 'answer', roundNo, optionIndex })
    },
    close() {
      closedByUser = true
      if (reconnectTimer) clearTimeout(reconnectTimer)
      socket?.close()
    }
  }
}
