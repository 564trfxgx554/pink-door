// Pink Door — 认证 API 层。
// 对接 Java（Spring Boot）后端。没有注册接口：
// 这是只属于一个人的私密站点，账号预置在后端数据库中
// （昵称即用户名，暗号即密码）。
//
// 后端契约（见项目根目录 ../docs/api.md）：
//   POST /api/auth/login
//   Request:  { "nickname": "昵称", "password": "暗号" }
//   Response: { "token": "JWT", "nickname": "昵称" }
//   Error:    401 { "message": "暗号不对哦" }
//   密码必须由后端做哈希（如 BCrypt），绝不保存明文，前端也不保存密码。

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 默认直连真实后端；只有显式设置 VITE_USE_MOCK_AUTH=true 才启用演示模式。
export const USE_MOCK_AUTH = import.meta.env.VITE_USE_MOCK_AUTH === 'true'

// ---------------------------------------------------------------------------
// 演示用 mock —— 仅在 VITE_USE_MOCK_AUTH=true 时启用。
// 任意非空昵称 + 暗号都会在约 0.9 秒后成功，方便没有后端时体验登录流程。
// ---------------------------------------------------------------------------
function mockLogin(nickname) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ token: 'demo-token', nickname })
    }, 900)
  })
}

export async function login(nickname, password) {
  if (!nickname || !password) {
    throw new Error('还没有告诉门，你是谁呀？先填一下昵称。')
  }

  if (USE_MOCK_AUTH) return mockLogin(nickname)

  let response
  try {
    response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      // 字段名必须与后端 LoginRequest 一致：nickname + password。
      body: JSON.stringify({ nickname, password })
    })
  } catch (error) {
    // 后端未启动或网络不通时直接报错，不再静默回退到 mock，
    // 否则会让人误以为已经连上后端。
    console.error('Login request failed:', error)
    throw new Error('门后面的服务器没有启动，请先启动后端服务。')
  }

  if (!response.ok) {
    // 401 表示昵称或暗号不匹配，交给登录页提示。
    throw new Error('暗号不对哦')
  }
  return await response.json()
}

export function logout() {
  sessionStorage.removeItem('pink-door-token')
  sessionStorage.removeItem('pink-door-nickname')
}
