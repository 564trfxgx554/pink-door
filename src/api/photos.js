// Pink Door — 相册 API 层。
// 前端只访问 Java；照片文件存在阿里云 OSS，浏览器直接读 OSS 的图片地址。

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function authHeaders() {
  const token = sessionStorage.getItem('pink-door-token')
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  }
}

// 上传用 FormData：Content-Type 不能手写，浏览器会自动带上 multipart 边界。
function uploadHeaders() {
  const token = sessionStorage.getItem('pink-door-token')
  return token ? { Authorization: `Bearer ${token}` } : {}
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

/**
 * 上传一张照片。
 * title 有值时一起传给后端，空时后端用文件名当标题。
 * 返回新照片 { id, url, title, takenAt, createdAt }。
 */
export async function uploadPhoto(file, title) {
  const form = new FormData()
  form.append('file', file)
  if (title) form.append('title', title)
  const response = await fetch(`${API_BASE_URL}/api/photos`, {
    method: 'POST',
    headers: uploadHeaders(),
    body: form
  })

  if (response.status === 401) {
    sessionStorage.removeItem('pink-door-token')
    throw new Error('登录已过期，请重新登录。')
  }
  if (!response.ok) {
    let message = '上传失败，请稍后再试。'
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
 * 改一张照片的名字。
 * 返回更新后的照片 { id, url, title, takenAt, createdAt }。
 */
export async function updatePhotoTitle(photoId, title) {
  const result = await request(`/api/photos/${photoId}`, {
    method: 'PATCH',
    body: JSON.stringify({ title })
  })
  return result.data
}

/**
 * 分页列出本空间的照片，按时间倒序。
 * 返回 { records, total }。
 */
export async function listPhotos(page = 1, size = 20) {
  const result = await request(`/api/photos?page=${page}&size=${size}`)
  const data = result.data || {}
  return { records: data.records || [], total: data.total || 0 }
}

export async function deletePhoto(photoId) {
  await request(`/api/photos/${photoId}`, { method: 'DELETE' })
}
