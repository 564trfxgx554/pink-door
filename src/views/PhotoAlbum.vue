<template>
  <div class="album">
    <Petals />
    <header class="topbar">
      <button class="back" @click="goHome">← 粉色小门</button>
      <div class="brand">📸 我们的相册</div>
      <div class="spacer" />
    </header>

    <main class="main">
      <h1 class="display">你拍的每一张，都好好收着 💕</h1>
      <p class="lead">旅行、生日、日常，都在这里啦</p>

      <section
        class="upload-card"
        :class="{ 'drag-over': dragOver }"
        @dragover.prevent="onDragOver"
        @dragleave="onDragLeave"
        @drop.prevent="onDrop"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          hidden
          @change="onFileChange"
        />
        <button class="pick" type="button" :disabled="uploading" @click="fileInput.click()">
          ＋ 选照片上传
        </button>
        <p class="upload-hint">支持 jpg、png、webp，单张不超过 5MB，也可以直接把照片拖进来</p>

        <div v-if="selectedFiles.length" class="pending">
          <div
            v-for="(item, index) in selectedFiles"
            :key="item.preview"
            class="pending-item"
          >
            <img :src="item.preview" alt="待上传的照片" />
            <div class="pending-body">
              <input
                v-model="item.title"
                class="pending-title"
                type="text"
                maxlength="100"
                placeholder="给这张照片起个名字"
                :disabled="item.status === 'uploading'"
              />
              <span class="pending-date">{{ item.date }}</span>
              <span v-if="item.status === 'uploading'" class="pending-state">上传中…</span>
              <span v-else-if="item.status === 'error'" class="pending-state error-text">{{ item.error }}</span>
            </div>
            <button
              v-if="item.status !== 'uploading'"
              type="button"
              class="pending-remove"
              :disabled="uploading"
              @click="removePending(index)"
            >×</button>
          </div>
        </div>

        <button
          v-if="readyCount"
          class="upload-btn"
          type="button"
          :disabled="uploading"
          @click="uploadSelected"
        >
          {{ uploading ? '正在上传…' : `上传 ${readyCount} 张` }}
        </button>
      </section>

      <p v-if="error" class="error" role="alert">{{ error }}</p>

      <div v-if="photos.length" class="sort-bar">
        <button
          type="button"
          class="sort-btn"
          :class="{ active: sortOrder === 'latest' }"
          @click="sortOrder = 'latest'"
        >最新在前</button>
        <button
          type="button"
          class="sort-btn"
          :class="{ active: sortOrder === 'earliest' }"
          @click="sortOrder = 'earliest'"
        >最早在前</button>
      </div>

      <div v-if="photos.length">
        <section
          v-for="group in groupedPhotos"
          :key="group.key"
          class="month-group"
        >
          <h2 class="month-title">{{ group.label }} · {{ group.photos.length }} 张</h2>
          <div class="grid">
            <article
              v-for="photo in group.photos"
              :key="photo.id"
              class="photo-card"
              @click="openViewer(photo)"
            >
              <img :src="photo.url" :alt="photo.title" loading="lazy" />
              <div class="photo-info">
                <span class="photo-title">{{ photo.title }}</span>
                <span class="photo-date">{{ shortDate(photo.takenAt) }}</span>
              </div>
              <button
                type="button"
                class="photo-delete"
                title="删除照片"
                :aria-label="`删除照片：${photo.title}`"
                :disabled="deletingId === photo.id"
                @click.stop="askDelete(photo)"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14M10 11v6M14 11v6" />
                </svg>
              </button>
            </article>
          </div>
        </section>
      </div>

      <p v-else-if="!loading" class="empty-hint">还没有照片，先上传第一张吧 💕</p>
      <p v-if="loading" class="empty-hint">正在翻开相册…</p>

      <button
        v-if="hasMore"
        class="more"
        type="button"
        :disabled="loadingMore"
        @click="loadMore"
      >
        {{ loadingMore ? '正在翻页…' : '再看看以前的照片' }}
      </button>

      <button class="next-card" type="button" @click="goGames">
        <span class="next-title">下一站：照片拼图 🧩</span>
        <span class="next-sub">把喜欢的照片拼起来，拼好就是一张回忆</span>
        <span class="next-arrow">去玩拼图 →</span>
      </button>
    </main>

    <div v-if="viewer" class="modal-mask viewer-mask" @click.self="closeViewer">
      <div class="viewer-card" role="dialog" aria-modal="true">
        <div class="viewer-image">
          <img :src="viewer.url" :alt="viewer.title" />
          <button
            type="button"
            class="viewer-nav prev"
            aria-label="上一张"
            @click="prevViewer"
          >‹</button>
          <button
            type="button"
            class="viewer-nav next"
            aria-label="下一张"
            @click="nextViewer"
          >›</button>
          <span class="viewer-count">{{ viewerIndex + 1 }} / {{ sortedPhotos.length }}</span>
        </div>
        <div class="viewer-info">
          <div class="viewer-text">
            <template v-if="!editingTitle">
              <h3>{{ viewer.title }}</h3>
              <p>{{ shortDate(viewer.takenAt) }}</p>
            </template>
            <template v-else>
              <input
                v-model="renameInput"
                class="rename-input"
                type="text"
                maxlength="100"
                placeholder="给这张照片起个名字"
                :disabled="renaming"
                @keyup.enter="saveRename"
                @keyup.esc="cancelRename"
              />
              <div class="rename-actions">
                <button type="button" class="rename-save" :disabled="renaming" @click="saveRename">
                  {{ renaming ? '保存中…' : '保存' }}
                </button>
                <button type="button" class="rename-cancel" :disabled="renaming" @click="cancelRename">取消</button>
              </div>
            </template>
            <p v-if="renameError" class="rename-error" role="alert">{{ renameError }}</p>
          </div>
          <div class="viewer-actions">
            <button type="button" class="viewer-puzzle" @click="playPuzzle">🧩 用这张玩拼图</button>
            <button
              type="button"
              class="viewer-rename"
              :disabled="editingTitle"
              @click="startRename"
            >✏️ 改名字</button>
            <button type="button" class="viewer-delete" :disabled="deletingId === viewer.id" @click="askDelete(viewer)">删除</button>
            <button type="button" class="viewer-close" @click="closeViewer">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="deleteTarget" class="modal-mask delete-mask" @click.self="cancelDelete">
      <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
        <div class="modal-bow" aria-hidden="true">🎀</div>
        <h3 id="delete-title">删除这张照片？</h3>
        <p>「{{ deleteTarget.title }}」删除后就找不回来了。</p>
        <div class="modal-actions">
          <button type="button" class="modal-cancel" @click="cancelDelete">再想想</button>
          <button
            type="button"
            class="modal-confirm"
            :disabled="deletingId !== null"
            @click="confirmDelete"
          >
            {{ deletingId !== null ? '删除中…' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'
import { deletePhoto, listPhotos, updatePhotoTitle, uploadPhoto } from '../api/photos.js'

const router = useRouter()
const photos = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const loadingMore = ref(false)
const uploading = ref(false)
const error = ref('')
const fileInput = ref(null)
const selectedFiles = ref([])
const dragOver = ref(false)
const sortOrder = ref('latest')
const viewerIndex = ref(null)
const deleteTarget = ref(null)
const deletingId = ref(null)
const editingTitle = ref(false)
const renameInput = ref('')
const renaming = ref(false)
const renameError = ref('')

const PAGE_SIZE = 20

const hasMore = computed(() => photos.value.length < total.value)
const readyCount = computed(() => selectedFiles.value.filter((item) => item.status === 'ready').length)

/**
 * 按当前排序排好的全部照片：最新在前按 takenAt 倒序，最早在前按 takenAt 正序。
 * takenAt 为空时退回 createdAt。
 */
const sortedPhotos = computed(() => {
  const list = [...photos.value]
  list.sort((a, b) => {
    const timeA = new Date(a.takenAt || a.createdAt || 0).getTime()
    const timeB = new Date(b.takenAt || b.createdAt || 0).getTime()
    return sortOrder.value === 'latest' ? timeB - timeA : timeA - timeB
  })
  return list
})

/** 按月分组：组按当前排序排，组内照片保持当前排序。 */
const groupedPhotos = computed(() => {
  const groups = new Map()
  for (const photo of sortedPhotos.value) {
    const key = (photo.takenAt || photo.createdAt || '').slice(0, 7)
    if (!groups.has(key)) {
      groups.set(key, { key, label: monthLabel(key), photos: [] })
    }
    groups.get(key).photos.push(photo)
  }
  return [...groups.values()]
})

const viewer = computed(() => {
  if (viewerIndex.value === null) return null
  return sortedPhotos.value[viewerIndex.value] || null
})

function showError(value) {
  error.value = value?.message || String(value)
}

function shortDate(value) {
  return value ? value.slice(0, 10) : ''
}

function todayDate() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function monthLabel(key) {
  if (!key || key.length < 7) return '其他时间'
  const [year, month] = key.split('-')
  return `${year}年${Number(month)}月`
}

function fileTitle(file) {
  const name = file.name || ''
  const dot = name.lastIndexOf('.')
  return dot > 0 ? name.slice(0, dot) : name
}

async function loadPhotos() {
  loading.value = true
  error.value = ''
  try {
    const result = await listPhotos(1, PAGE_SIZE)
    photos.value = result.records
    total.value = result.total
    page.value = 1
  } catch (value) {
    showError(value)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const result = await listPhotos(page.value + 1, PAGE_SIZE)
    photos.value = photos.value.concat(result.records)
    total.value = result.total
    page.value += 1
  } catch (value) {
    showError(value)
  } finally {
    loadingMore.value = false
  }
}

function addFiles(fileList) {
  const files = Array.from(fileList || []).filter((file) => file.type.startsWith('image/'))
  for (const file of files) {
    selectedFiles.value.push({
      file,
      preview: URL.createObjectURL(file),
      title: fileTitle(file),
      date: todayDate(),
      status: 'ready',
      error: ''
    })
  }
}

function onFileChange(event) {
  addFiles(event.target.files)
  // 清空选择，同一张照片下次还能再选。
  event.target.value = ''
}

function onDragOver(event) {
  if (event.dataTransfer && [...event.dataTransfer.types].includes('Files')) {
    dragOver.value = true
  }
}

function onDragLeave() {
  dragOver.value = false
}

function onDrop(event) {
  dragOver.value = false
  addFiles(event.dataTransfer.files)
}

function removePending(index) {
  const item = selectedFiles.value[index]
  if (item) URL.revokeObjectURL(item.preview)
  selectedFiles.value.splice(index, 1)
}

async function uploadSelected() {
  if (uploading.value || !readyCount.value) return
  uploading.value = true
  error.value = ''
  for (const item of selectedFiles.value) {
    if (item.status !== 'ready') continue
    item.status = 'uploading'
    try {
      const title = item.title.trim()
      const created = await uploadPhoto(item.file, title || undefined)
      item.status = 'done'
      photos.value.unshift(created)
      total.value += 1
    } catch (value) {
      item.status = 'error'
      item.error = value?.message || '上传失败'
    }
  }
  // 传好的从待上传里清掉，本地预览地址一起释放。
  const done = selectedFiles.value.filter((item) => item.status === 'done')
  for (const item of done) URL.revokeObjectURL(item.preview)
  selectedFiles.value = selectedFiles.value.filter((item) => item.status !== 'done')
  uploading.value = false
}

function openViewer(photo) {
  viewerIndex.value = sortedPhotos.value.indexOf(photo)
  editingTitle.value = false
  renameError.value = ''
}

function closeViewer() {
  viewerIndex.value = null
  editingTitle.value = false
  renameError.value = ''
}

function prevViewer() {
  if (!sortedPhotos.value.length) return
  viewerIndex.value = (viewerIndex.value - 1 + sortedPhotos.value.length) % sortedPhotos.value.length
}

function nextViewer() {
  if (!sortedPhotos.value.length) return
  viewerIndex.value = (viewerIndex.value + 1) % sortedPhotos.value.length
}

function playPuzzle() {
  if (!viewer.value) return
  sessionStorage.setItem('pink-door-puzzle-photo', JSON.stringify({
    id: viewer.value.id,
    url: viewer.value.url,
    title: viewer.value.title
  }))
  router.push('/games/puzzle')
}

function startRename() {
  if (!viewer.value) return
  renameInput.value = viewer.value.title
  renameError.value = ''
  editingTitle.value = true
}

function cancelRename() {
  editingTitle.value = false
  renameError.value = ''
}

async function saveRename() {
  if (!viewer.value || renaming.value) return
  const title = renameInput.value.trim()
  if (!title) {
    renameError.value = '名字不能为空。'
    return
  }
  if (title === viewer.value.title) {
    editingTitle.value = false
    return
  }
  renaming.value = true
  renameError.value = ''
  try {
    const updated = await updatePhotoTitle(viewer.value.id, title)
    const target = photos.value.find((item) => item.id === updated.id)
    if (target) target.title = updated.title
    editingTitle.value = false
  } catch (value) {
    renameError.value = value?.message || '改名失败，请稍后再试。'
  } finally {
    renaming.value = false
  }
}

function askDelete(photo) {
  deleteTarget.value = photo
}

function cancelDelete() {
  if (deletingId.value !== null) return
  deleteTarget.value = null
}

async function confirmDelete() {
  const target = deleteTarget.value
  if (!target || deletingId.value !== null) return

  deletingId.value = target.id
  try {
    await deletePhoto(target.id)
    photos.value = photos.value.filter((item) => item.id !== target.id)
    total.value = Math.max(total.value - 1, 0)
    deleteTarget.value = null
    if (viewer.value && viewer.value.id === target.id) {
      closeViewer()
    } else if (viewerIndex.value !== null && viewerIndex.value >= sortedPhotos.value.length) {
      // 删掉的是查看器后面的照片，序号越界时回到最后一张。
      viewerIndex.value = Math.max(sortedPhotos.value.length - 1, 0)
    }
  } catch (value) {
    showError(value)
  } finally {
    deletingId.value = null
  }
}

function goHome() {
  router.push('/home')
}

function goGames() {
  router.push('/games')
}

onMounted(loadPhotos)
</script>

<style scoped>
.album { min-height: 100vh; position: relative; overflow: hidden; }
.topbar { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: center; padding: 18px 28px; }
.back { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 8px 16px; font-weight: 700; }
.brand { font-size: 22px; font-weight: 800; color: var(--pink-600); }
.spacer { width: 92px; }
.main { position: relative; z-index: 2; max-width: 960px; margin: 0 auto; padding: 12px 20px 48px; text-align: center; }
.main h1 { font-size: 30px; color: var(--pink-600); margin: 8px 0; }
.lead { color: var(--muted); font-size: 16px; margin: 0 0 20px; }

.upload-card { background: var(--card); border: 1px dashed var(--pink-300); border-radius: 24px; box-shadow: var(--shadow); padding: 22px 20px; margin-bottom: 24px; transition: border-color 0.18s ease, background 0.18s ease; }
.upload-card.drag-over { border-color: var(--pink-500); border-style: solid; background: var(--pink-pale); }
.pick { background: var(--pink-500); color: #fff; border: none; border-radius: 999px; padding: 11px 22px; font-weight: 700; font-size: 15px; }
.pick:disabled { opacity: 0.6; cursor: wait; }
.upload-hint { color: var(--muted); font-size: 13px; margin: 10px 0 0; }
.pending { display: flex; flex-direction: column; gap: 12px; margin-top: 18px; text-align: left; }
.pending-item { position: relative; display: flex; gap: 12px; align-items: flex-start; background: var(--pink-pale); border: 1px solid var(--pink-200); border-radius: 16px; padding: 10px; }
.pending-item img { width: 120px; height: 90px; object-fit: cover; border-radius: 12px; border: 1px solid var(--pink-200); display: block; flex: none; }
.pending-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.pending-title { width: 100%; box-sizing: border-box; border: 1px solid var(--pink-200); border-radius: 10px; background: var(--card); color: var(--ink); font-size: 14px; font-weight: 700; padding: 7px 10px; }
.pending-title:focus { outline: none; border-color: var(--pink-500); }
.pending-date { font-size: 12px; color: var(--muted); }
.pending-state { font-size: 12px; color: var(--pink-600); font-weight: 700; }
.error-text { color: #c0392b; }
.pending-remove { position: absolute; top: -8px; right: -8px; width: 22px; height: 22px; border-radius: 50%; border: none; background: var(--pink-500); color: #fff; font-size: 14px; line-height: 1; }
.upload-btn { margin-top: 16px; background: linear-gradient(135deg, var(--pink-400), var(--pink-500)); color: #fff; border: none; border-radius: 999px; padding: 11px 24px; font-weight: 700; box-shadow: 0 8px 18px rgba(217, 54, 110, 0.24); }
.upload-btn:disabled { opacity: 0.6; cursor: wait; }

.error { color: #c0392b; background: #fdecea; border-radius: 12px; padding: 10px 12px; font-size: 13px; margin: 0 0 16px; text-align: left; }

.sort-bar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 16px; }
.sort-btn { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 7px 16px; font-weight: 700; font-size: 13px; }
.sort-btn.active { background: var(--pink-500); border-color: var(--pink-500); color: #fff; }

.month-group { text-align: left; margin-bottom: 28px; }
.month-title { font-size: 18px; font-weight: 800; color: var(--pink-600); margin: 0 0 12px; }

.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; text-align: left; }
.photo-card { position: relative; background: var(--card); border: 1px solid var(--pink-200); border-radius: 22px; box-shadow: var(--shadow); overflow: hidden; cursor: pointer; transition: transform 0.18s ease; }
.photo-card:hover { transform: translateY(-3px); }
.photo-card img { width: 100%; aspect-ratio: 4 / 3; object-fit: cover; display: block; }
.photo-info { padding: 12px 14px; display: flex; flex-direction: column; gap: 2px; }
.photo-title { font-size: 14px; font-weight: 700; color: var(--pink-600); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.photo-date { font-size: 12px; color: var(--muted); }
.photo-delete { position: absolute; top: 10px; right: 10px; width: 30px; height: 30px; display: grid; place-items: center; border: none; border-radius: 10px; background: rgba(255, 250, 253, 0.9); color: var(--ink-soft); opacity: 0; transition: opacity 0.18s ease, color 0.18s ease; }
.photo-delete svg { width: 16px; height: 16px; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
.photo-card:hover .photo-delete, .photo-delete:focus-visible { opacity: 1; }
.photo-delete:hover:not(:disabled) { color: var(--pink-deep); }
.photo-delete:disabled { opacity: 0.4; cursor: wait; }
@media (hover: none) {
  .photo-delete { opacity: 1; }
}
.empty-hint { color: var(--muted); font-size: 14px; margin: 28px 0; }
.more { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 10px 22px; font-weight: 700; margin-top: 8px; }
.more:disabled { opacity: 0.6; cursor: wait; }

.next-card { display: block; width: 100%; box-sizing: border-box; margin-top: 32px; padding: 22px 24px; text-align: left; border: 1px solid var(--pink-300); border-radius: 24px; background: linear-gradient(135deg, var(--pink-soft), var(--card)); box-shadow: var(--shadow); color: var(--ink); cursor: pointer; transition: transform 0.18s ease; }
.next-card:hover { transform: translateY(-3px); }
.next-title { display: block; font-size: 18px; font-weight: 800; color: var(--pink-600); }
.next-sub { display: block; font-size: 13px; color: var(--muted); margin-top: 4px; }
.next-arrow { display: block; font-size: 14px; font-weight: 700; color: var(--pink-500); margin-top: 10px; }

.modal-mask { position: fixed; inset: 0; z-index: 30; display: grid; place-items: center; padding: 20px; background: rgba(91, 42, 63, 0.3); backdrop-filter: blur(4px); }
.delete-mask { z-index: 40; }
.viewer-card { width: min(720px, 100%); background: var(--card); border: 1px solid var(--card-border); border-radius: 24px; box-shadow: var(--shadow); overflow: hidden; animation: pop 0.22s ease; }
.viewer-image { position: relative; background: var(--pink-pale); }
.viewer-image img { width: 100%; max-height: 70vh; object-fit: contain; display: block; }
.viewer-nav { position: absolute; top: 50%; transform: translateY(-50%); width: 40px; height: 40px; border-radius: 50%; border: none; background: rgba(255, 250, 253, 0.9); color: var(--pink-600); font-size: 26px; line-height: 1; font-weight: 700; box-shadow: var(--shadow); }
.viewer-nav.prev { left: 12px; }
.viewer-nav.next { right: 12px; }
.viewer-count { position: absolute; bottom: 12px; right: 12px; background: rgba(91, 42, 63, 0.55); color: #fff; font-size: 12px; font-weight: 700; border-radius: 999px; padding: 4px 10px; }
.viewer-info { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; padding: 16px 20px; text-align: left; }
.viewer-text { flex: 1; min-width: 0; }
.viewer-info h3 { margin: 0 0 4px; color: var(--pink-600); font-size: 18px; }
.viewer-info p { margin: 0; color: var(--muted); font-size: 13px; }
.viewer-actions { display: flex; flex-wrap: wrap; gap: 8px; flex: none; justify-content: flex-end; }
.viewer-puzzle { border: none; background: linear-gradient(135deg, var(--pink-400), var(--pink-500)); color: #fff; border-radius: 999px; padding: 9px 16px; font-weight: 700; }
.viewer-rename { border: 1px solid var(--card-border); background: var(--pink-pale); color: var(--pink-deep); border-radius: 999px; padding: 9px 16px; font-weight: 700; }
.viewer-rename:disabled { opacity: 0.6; }
.viewer-delete { border: 1px solid var(--card-border); background: var(--pink-pale); color: var(--pink-deep); border-radius: 999px; padding: 9px 18px; font-weight: 700; }
.viewer-close { border: none; background: var(--pink-500); color: #fff; border-radius: 999px; padding: 9px 18px; font-weight: 700; }
.rename-input { width: 100%; box-sizing: border-box; border: 1px solid var(--pink-500); border-radius: 10px; background: var(--card); color: var(--ink); font-size: 16px; font-weight: 700; padding: 8px 10px; }
.rename-input:focus { outline: none; }
.rename-actions { display: flex; gap: 8px; margin-top: 8px; }
.rename-save { border: none; background: var(--pink-500); color: #fff; border-radius: 999px; padding: 7px 16px; font-weight: 700; }
.rename-save:disabled { opacity: 0.6; }
.rename-cancel { border: 1px solid var(--card-border); background: var(--pink-pale); color: var(--ink); border-radius: 999px; padding: 7px 16px; font-weight: 700; }
.rename-error { color: #c0392b !important; font-size: 13px; margin-top: 8px !important; }
.modal-card { width: min(380px, 100%); padding: 28px 24px 22px; border: 1px solid var(--card-border); border-radius: 24px; background: var(--card); box-shadow: var(--shadow); text-align: center; animation: pop 0.22s ease; }
.modal-bow { margin-top: -50px; margin-bottom: 4px; font-size: 32px; animation: wiggle 2.5s ease-in-out infinite; }
.modal-card h3 { margin: 0 0 8px; color: var(--pink-deep); font-size: 20px; }
.modal-card p { margin: 0 0 20px; color: var(--ink-soft); font-size: 14px; line-height: 1.6; word-break: break-word; }
.modal-actions { display: flex; gap: 10px; }
.modal-actions button { flex: 1; height: 42px; border-radius: 13px; font-size: 14px; font-weight: 700; }
.modal-cancel { border: 1px solid var(--card-border); background: var(--pink-pale); color: var(--ink); }
.modal-confirm { border: none; background: linear-gradient(135deg, var(--pink-400), var(--pink-500)); color: #fff; box-shadow: 0 8px 18px rgba(217, 54, 110, 0.24); }
.modal-confirm:disabled { opacity: 0.6; cursor: wait; }

@media (max-width: 760px) {
  .grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .main h1 { font-size: 24px; }
  .spacer { display: none; }
  .viewer-info { flex-direction: column; align-items: flex-start; }
  .viewer-actions { justify-content: flex-start; }
  .pending-item img { width: 96px; height: 72px; }
}
</style>
