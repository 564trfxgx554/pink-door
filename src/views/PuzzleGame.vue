<template>
  <div class="puzzle">
    <Petals />
    <header class="topbar">
      <button class="back" @click="goGames">← 游戏列表</button>
      <div class="brand">🧩 照片拼图</div>
      <div class="spacer" />
    </header>

    <main class="main">
      <h1 class="display">把我们的照片拼回来吧 🎀</h1>
      <p class="lead">点一下空位旁边的图块，它就会滑过去，拼好整张照片</p>

      <div class="toolbar">
        <div class="stats">
          <div class="stat"><span class="label">步数</span><strong>{{ moves }}</strong></div>
          <div class="stat"><span class="label">时间</span><strong>{{ formattedTime }}</strong></div>
        </div>
        <div class="actions">
          <button type="button" class="ghost" :disabled="loadingPhoto" @click="openPicker">🖼 选择照片</button>
          <label class="level">
            <span>难度</span>
            <select v-model.number="size" :disabled="!photo" @change="newGame">
              <option :value="3">轻松 3×3</option>
              <option :value="4">挑战 4×4</option>
              <option :value="5">高手 5×5</option>
            </select>
          </label>
          <button type="button" class="restart" :disabled="!photo" @click="newGame">重新开始</button>
        </div>
      </div>

      <p v-if="imageError" class="error" role="alert">{{ imageError }}</p>

      <section v-if="!photo" class="empty">
        <div class="empty-icon">🖼</div>
        <h2>还没有选照片哦</h2>
        <p>{{ loadingPhoto ? '正在打开照片…' : '从我们的相册里挑一张，就可以开始拼啦' }}</p>
        <div class="empty-actions">
          <button type="button" class="primary" @click="openPicker">从相册选一张</button>
          <button type="button" class="secondary" @click="goAlbum">去相册上传</button>
        </div>
      </section>

      <template v-else>
        <p class="photo-name">正在拼：{{ photo.title }}</p>
        <div
          class="board"
          :style="{ aspectRatio: ratio || 1, '--n': size, '--r': ratio || 1 }"
          :class="{ solved: won }"
        >
          <button
            v-for="(tile, slot) in board"
            :key="`${size}-${slot}`"
            type="button"
            class="tile"
            :class="{ blank: tile === blankId, movable: isMovable(slot) && !won }"
            :style="tileStyle(tile)"
            :disabled="tile === blankId || won"
            :aria-label="tile === blankId ? '空位' : `图块 ${tile + 1}`"
            @click="move(slot)"
          />
        </div>
        <p class="hint">小提示：先拼好四个角，再慢慢填中间，会快很多哦 💕</p>
      </template>
    </main>

    <div v-if="pickerOpen" class="picker-overlay" @click.self="closePicker">
      <div class="picker-card">
        <header class="picker-head">
          <h2>从相册里选一张</h2>
          <button type="button" class="picker-close" aria-label="关闭" @click="closePicker">×</button>
        </header>

        <p v-if="pickerError" class="error" role="alert">{{ pickerError }}</p>

        <div v-if="pickerLoading && !pickerPhotos.length" class="picker-state">相册加载中…</div>
        <div v-else-if="!pickerPhotos.length" class="picker-state">
          <p>相册里还没有照片，先去上传一张吧。</p>
          <button type="button" class="primary" @click="goAlbum">去相册上传</button>
        </div>
        <div v-else class="picker-grid">
          <button
            v-for="item in pickerPhotos"
            :key="item.id"
            type="button"
            class="picker-item"
            :class="{ active: photo && photo.id === item.id }"
            @click="pickPhoto(item)"
          >
            <img :src="item.url" :alt="item.title" loading="lazy" />
            <span>{{ item.title }}</span>
          </button>
        </div>

        <button
          v-if="pickerPhotos.length < pickerTotal"
          type="button"
          class="ghost load-more"
          :disabled="pickerLoading"
          @click="loadPickerPage(pickerPage + 1)"
        >
          {{ pickerLoading ? '加载中…' : '加载更多' }}
        </button>
      </div>
    </div>

    <div v-if="won" class="win-overlay">
      <div class="win-card">
        <div class="win-icon">🎉</div>
        <h2 class="display">拼好啦，回忆又完整了一张！</h2>
        <p>用时 {{ formattedTime }}，共 {{ moves }} 步，好厉害呀 💖</p>
        <div class="win-actions">
          <button type="button" class="primary" @click="newGame">再拼一次</button>
          <button type="button" class="secondary" @click="openPicker">换一张照片</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'
import { listPhotos } from '../api/photos'

// 相册「用这张玩拼图」写入的照片，和 PhotoAlbum.vue 保持同一个 key。
const PUZZLE_PHOTO_KEY = 'pink-door-puzzle-photo'
const PICKER_PAGE_SIZE = 12

const router = useRouter()

const size = ref(3)
const photo = ref(null)
const ratio = ref(1)
const board = ref([])
const moves = ref(0)
const seconds = ref(0)
const started = ref(false)
const won = ref(false)
const imageError = ref('')
const loadingPhoto = ref(false)

const pickerOpen = ref(false)
const pickerPhotos = ref([])
const pickerPage = ref(1)
const pickerTotal = ref(0)
const pickerLoading = ref(false)
const pickerError = ref('')

let timer = null

const blankId = computed(() => size.value * size.value - 1)

const formattedTime = computed(() => {
  const m = Math.floor(seconds.value / 60)
  const s = seconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

/** 图块的背景样式：放大成整张图的 size 倍，再按原位偏移，实现切块效果。 */
function tileStyle(tile) {
  if (!photo.value || tile === blankId.value) return {}
  const n = size.value
  const col = tile % n
  const row = Math.floor(tile / n)
  return {
    backgroundImage: `url("${photo.value.url}")`,
    backgroundSize: `${n * 100}% ${n * 100}%`,
    backgroundPosition: `${(col * 100) / (n - 1)}% ${(row * 100) / (n - 1)}%`
  }
}

/** 与空位上下左右相邻的格子编号。 */
function neighborIndexes(slot) {
  const n = size.value
  const row = Math.floor(slot / n)
  const col = slot % n
  const result = []
  if (row > 0) result.push(slot - n)
  if (row < n - 1) result.push(slot + n)
  if (col > 0) result.push(slot - 1)
  if (col < n - 1) result.push(slot + 1)
  return result
}

function isMovable(slot) {
  const blank = board.value.indexOf(blankId.value)
  return blank >= 0 && neighborIndexes(slot).includes(blank)
}

/** 从完成态随机走合法的步生成局面，保证一定可解。 */
function buildShuffledBoard() {
  const n = size.value * size.value
  const slots = Array.from({ length: n }, (_, index) => index)
  let blank = n - 1
  let prev = -1
  for (let step = 0; step < n * 80; step++) {
    const options = neighborIndexes(blank).filter((index) => index !== prev)
    const pick = options[Math.floor(Math.random() * options.length)]
    ;[slots[blank], slots[pick]] = [slots[pick], slots[blank]]
    prev = blank
    blank = pick
  }
  // 极小概率走回完成态，强制再走一步。
  if (slots.every((tile, index) => tile === index)) {
    const pick = neighborIndexes(blank)[0]
    ;[slots[blank], slots[pick]] = [slots[pick], slots[blank]]
  }
  return slots
}

function newGame() {
  if (!photo.value) return
  stopTimer()
  board.value = buildShuffledBoard()
  moves.value = 0
  seconds.value = 0
  started.value = false
  won.value = false
}

function move(slot) {
  if (won.value || slot === board.value.indexOf(blankId.value)) return
  if (!isMovable(slot)) return
  const blank = board.value.indexOf(blankId.value)
  const next = [...board.value]
  next[blank] = next[slot]
  next[slot] = blankId.value
  board.value = next
  if (!started.value) startTimer()
  moves.value += 1
  if (board.value.every((tile, index) => tile === index)) {
    stopTimer()
    won.value = true
  }
}

function startTimer() {
  if (started.value) return
  started.value = true
  timer = setInterval(() => { seconds.value += 1 }, 1000)
}

function stopTimer() {
  if (timer) clearInterval(timer)
  timer = null
}

/** 先加载图片拿到原始比例，再铺棋盘，避免图块被拉变形。 */
function applyPhoto(item) {
  if (!item || !item.url) return
  loadingPhoto.value = true
  imageError.value = ''
  const image = new Image()
  image.onload = () => {
    photo.value = item
    ratio.value = image.naturalHeight ? image.naturalWidth / image.naturalHeight : 1
    loadingPhoto.value = false
    newGame()
  }
  image.onerror = () => {
    photo.value = null
    loadingPhoto.value = false
    imageError.value = '这张照片加载失败了，换一张试试。'
    pickerOpen.value = true
  }
  image.src = item.url
}

function openPicker() {
  pickerOpen.value = true
  loadPickerPage(1)
}

function closePicker() {
  pickerOpen.value = false
}

async function loadPickerPage(page) {
  if (pickerLoading.value) return
  pickerLoading.value = true
  pickerError.value = ''
  try {
    const result = await listPhotos(page, PICKER_PAGE_SIZE)
    pickerPhotos.value = page === 1 ? result.records : pickerPhotos.value.concat(result.records)
    pickerTotal.value = result.total
    pickerPage.value = page
  } catch (value) {
    pickerError.value = value?.message || '相册加载失败，请稍后再试。'
  } finally {
    pickerLoading.value = false
  }
}

function pickPhoto(item) {
  pickerOpen.value = false
  applyPhoto(item)
}

function goGames() {
  router.push('/games')
}

function goAlbum() {
  router.push('/photos')
}

onMounted(() => {
  const raw = sessionStorage.getItem(PUZZLE_PHOTO_KEY)
  if (!raw) {
    openPicker()
    return
  }
  try {
    applyPhoto(JSON.parse(raw))
  } catch {
    openPicker()
  }
})

onUnmounted(stopTimer)
</script>

<style scoped>
.puzzle { min-height: 100vh; position: relative; overflow: hidden; }
.topbar { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: center; padding: 18px 28px; }
.back { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 8px 16px; font-weight: 700; }
.brand { font-size: 22px; font-weight: 800; color: var(--pink-600); }
.spacer { width: 92px; }
.main { position: relative; z-index: 2; max-width: 720px; margin: 0 auto; padding: 12px 20px 48px; text-align: center; }
.main h1 { font-size: 30px; color: var(--pink-600); margin: 8px 0; }
.lead { color: var(--muted); font-size: 16px; margin: 0 0 18px; }

.toolbar { display: flex; flex-wrap: wrap; gap: 12px; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.stats { display: flex; gap: 10px; }
.stat { background: var(--card); border: 1px solid var(--pink-200); border-radius: 16px; padding: 6px 14px; min-width: 76px; }
.stat .label { display: block; font-size: 12px; color: var(--muted); }
.stat strong { font-size: 19px; color: var(--pink-600); }
.actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.ghost { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 9px 16px; font-weight: 700; }
.ghost:disabled { opacity: 0.6; cursor: wait; }
.restart { background: var(--pink-500); color: #fff; border: none; border-radius: 999px; padding: 10px 18px; font-weight: 700; }
.restart:disabled { opacity: 0.6; }
.level { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--muted); }
.level select { background: var(--card); border: 1px solid var(--pink-200); border-radius: 999px; color: var(--ink); padding: 8px 10px; font-weight: 700; }

.photo-name { color: var(--muted); font-size: 14px; margin: 0 0 12px; }
.board {
  display: grid;
  /* 之前缺了这两行，grid 默认只有 1 列，9 个图块全挤成竖条；
     现在按难度分 N 列 N 行，图块正好是棋盘的 1/N，背景切片才对得上。 */
  grid-template-columns: repeat(var(--n, 3), 1fr);
  grid-template-rows: repeat(var(--n, 3), 1fr);
  gap: 3px;
  /* 72vh 这一项是给很高的竖图限高，免得棋盘高出屏幕；正常照片还是按 480px 走。 */
  width: min(480px, 100%, calc(72vh * var(--r, 1)));
  margin: 0 auto;
  padding: 6px;
  border-radius: 20px;
  background: var(--pink-100);
  border: 1px solid var(--pink-200);
  box-shadow: var(--shadow);
}
.board.solved { outline: 4px solid var(--pink-400); }
.tile {
  min-width: 0;
  min-height: 0;
  padding: 0;
  border: none;
  border-radius: 4px;
  background-repeat: no-repeat;
  background-color: var(--card);
  cursor: default;
  transition: transform 0.08s ease;
}
.tile.movable { cursor: pointer; }
.tile.movable:hover { transform: scale(0.96); }
.tile.blank { background: var(--pink-pale); border-radius: 8px; }
.hint { margin-top: 18px; color: var(--muted); font-size: 14px; }
.error { color: #c0392b !important; }

.empty { background: var(--card); border: 1px dashed var(--pink-300); border-radius: 24px; padding: 36px 20px; box-shadow: var(--shadow); }
.empty-icon { font-size: 46px; }
.empty h2 { color: var(--pink-600); margin: 10px 0 6px; }
.empty p { color: var(--muted); margin: 0 0 18px; }
.empty-actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
.primary { background: var(--pink-500); color: #fff; border: none; border-radius: 999px; padding: 11px 20px; font-weight: 700; }
.secondary { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 11px 20px; font-weight: 700; }

.picker-overlay { position: fixed; inset: 0; z-index: 40; background: rgba(92, 36, 56, 0.45); display: flex; align-items: center; justify-content: center; padding: 20px; }
.picker-card { background: var(--card); border-radius: 26px; padding: 22px; width: min(640px, 100%); max-height: 86vh; overflow-y: auto; box-shadow: var(--shadow); text-align: left; }
.picker-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.picker-head h2 { margin: 0; font-size: 20px; color: var(--pink-600); }
.picker-close { border: none; background: var(--pink-100); color: var(--pink-600); border-radius: 999px; width: 34px; height: 34px; font-size: 20px; font-weight: 700; }
.picker-state { text-align: center; color: var(--muted); padding: 28px 10px; }
.picker-state p { margin: 0 0 14px; }
.picker-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.picker-item { border: 2px solid transparent; background: var(--pink-pale); border-radius: 14px; padding: 0; overflow: hidden; cursor: pointer; text-align: left; }
.picker-item.active { border-color: var(--pink-500); }
.picker-item img { width: 100%; aspect-ratio: 1; object-fit: cover; display: block; }
.picker-item span { display: block; padding: 6px 8px; font-size: 12px; color: var(--ink); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.load-more { margin-top: 16px; width: 100%; }

.win-overlay { position: fixed; inset: 0; z-index: 30; background: rgba(92, 36, 56, 0.45); display: flex; align-items: center; justify-content: center; padding: 20px; }
.win-card { background: var(--card); border-radius: 28px; padding: 36px 28px; max-width: 440px; width: 100%; text-align: center; box-shadow: var(--shadow); animation: pop 0.3s ease; }
.win-icon { font-size: 52px; }
.win-card h2 { color: var(--pink-600); font-size: 26px; margin: 10px 0; }
.win-card p { color: var(--muted); margin: 0 0 22px; }
.win-actions { display: flex; gap: 12px; justify-content: center; }

@media (max-width: 560px) {
  .main h1 { font-size: 24px; }
  .toolbar { justify-content: center; }
  .spacer { display: none; }
  .picker-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
