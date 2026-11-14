<template>
  <div class="memory">
    <Petals />
    <header class="topbar">
      <button class="back" @click="goGames">← 游戏列表</button>
      <div class="brand">🃏 照片记忆翻牌</div>
      <div class="spacer" />
    </header>

    <main class="main">
      <h1 class="display">翻开两张一样的，就配对成功啦 🎀</h1>
      <p class="lead">三丽鸥的小伙伴们藏在牌后面，记住它们的位置哦</p>

      <div class="stats">
        <div class="stat"><span class="label">步数</span><strong>{{ steps }}</strong></div>
        <div class="stat"><span class="label">已配对</span><strong>{{ matchedCount }} / 8</strong></div>
        <div class="stat"><span class="label">时间</span><strong>{{ formattedTime }}</strong></div>
        <button class="restart" @click="startGame">重新开始</button>
      </div>

      <div class="board">
        <button
          v-for="card in cards"
          :key="card.uid"
          class="card"
          :class="{ flipped: card.flipped || card.matched, matched: card.matched, shake: card.shake }"
          :disabled="card.flipped || card.matched || locked || won"
          @click="flipCard(card)"
        >
          <span class="face back-face">🎀</span>
          <span class="face front-face">
            <span class="emoji">{{ card.emoji }}</span>
            <span class="name">{{ card.name }}</span>
          </span>
        </button>
      </div>

      <p class="hint">小提示：先翻一张记住，再找它的另一半，步数越少越厉害哦 💕</p>
    </main>

    <div v-if="won" class="win-overlay">
      <div class="win-card">
        <div class="win-icon">🎉</div>
        <h2 class="display">你们的回忆又拼好了一张！</h2>
        <p>用时 {{ formattedTime }}，共 {{ steps }} 步，太默契啦 💖</p>
        <div class="win-actions">
          <button class="primary" @click="startGame">再玩一次</button>
          <button class="secondary" @click="goGames">回游戏列表</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'

const router = useRouter()

// 三丽鸥风占位牌面：先用可爱角色 / 元素代替真实照片，
// 等相册接好后，这里换成 GET /api/photos 拿到的照片 URL。
const PAIRS = [
  { id: 1, emoji: '🎀', name: 'Hello Kitty' },
  { id: 2, emoji: '🐰', name: '美乐蒂' },
  { id: 3, emoji: '🐶', name: '玉桂狗' },
  { id: 4, emoji: '🐾', name: '布丁狗' },
  { id: 5, emoji: '😈', name: '库洛米' },
  { id: 6, emoji: '⭐', name: '双子星' },
  { id: 7, emoji: '🌸', name: '樱花' },
  { id: 8, emoji: '🍓', name: '草莓' }
]

const cards = ref([])
const steps = ref(0)
const matchedCount = ref(0)
const locked = ref(false)
const won = ref(false)
const seconds = ref(0)
const started = ref(false)
let timer = null
let firstCard = null

const formattedTime = computed(() => {
  const m = Math.floor(seconds.value / 60)
  const s = seconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

function startGame() {
  stopTimer()
  const deck = shuffle([...PAIRS, ...PAIRS]).map((pair, index) => ({
    ...pair,
    uid: index,
    flipped: false,
    matched: false,
    shake: false
  }))
  cards.value = deck
  steps.value = 0
  matchedCount.value = 0
  locked.value = false
  won.value = false
  seconds.value = 0
  started.value = false
  firstCard = null
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

function flipCard(card) {
  if (locked.value || card.flipped || card.matched) return
  startTimer()
  card.flipped = true

  if (!firstCard) {
    firstCard = card
    return
  }

  steps.value += 1
  const secondCard = card

  if (firstCard.id === secondCard.id) {
    firstCard.matched = true
    secondCard.matched = true
    matchedCount.value += 1
    firstCard = null
    if (matchedCount.value === PAIRS.length) {
      stopTimer()
      won.value = true
    }
    return
  }

  locked.value = true
  const a = firstCard
  const b = secondCard
  firstCard = null
  a.shake = true
  b.shake = true
  setTimeout(() => {
    a.flipped = false
    b.flipped = false
    a.shake = false
    b.shake = false
    locked.value = false
  }, 800)
}

function goGames() {
  router.push('/games')
}

onUnmounted(stopTimer)
startGame()
</script>

<style scoped>
.memory { min-height: 100vh; position: relative; overflow: hidden; }
.topbar { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: center; padding: 18px 28px; }
.back { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 8px 16px; font-weight: 700; }
.brand { font-size: 22px; font-weight: 800; color: var(--pink-600); }
.spacer { width: 92px; }
.main { position: relative; z-index: 2; max-width: 720px; margin: 0 auto; padding: 12px 20px 48px; text-align: center; }
.main h1 { font-size: 30px; color: var(--pink-600); margin: 8px 0; }
.lead { color: var(--muted); font-size: 16px; margin: 0 0 20px; }
.stats { display: flex; gap: 12px; justify-content: center; align-items: center; margin-bottom: 20px; flex-wrap: wrap; }
.stat { background: var(--card); border: 1px solid var(--pink-200); border-radius: 16px; padding: 8px 16px; min-width: 88px; }
.stat .label { display: block; font-size: 12px; color: var(--muted); }
.stat strong { font-size: 20px; color: var(--pink-600); }
.restart { background: var(--pink-500); color: #fff; border: none; border-radius: 999px; padding: 10px 18px; font-weight: 700; }
.board { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; max-width: 560px; margin: 0 auto; }
.card { position: relative; aspect-ratio: 3 / 4; border: none; background: transparent; padding: 0; perspective: 600px; }
.face { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; border-radius: 18px; backface-visibility: hidden; transition: transform 0.45s ease; box-shadow: var(--shadow); }
.back-face { background: linear-gradient(135deg, var(--pink-400), var(--pink-600)); color: #fff; font-size: 36px; transform: rotateY(0deg); }
.front-face { background: var(--card); border: 2px solid var(--pink-200); transform: rotateY(180deg); }
.card.flipped .back-face { transform: rotateY(180deg); }
.card.flipped .front-face { transform: rotateY(0deg); }
.card.matched .front-face { border-color: var(--pink-400); background: var(--pink-100); }
.emoji { font-size: 38px; }
.name { font-size: 12.5px; font-weight: 700; color: var(--pink-600); }
.card.shake { animation: shake 0.4s ease; }
.hint { margin-top: 20px; color: var(--muted); font-size: 14px; }
.win-overlay { position: fixed; inset: 0; z-index: 10; background: rgba(92, 36, 56, 0.45); display: flex; align-items: center; justify-content: center; padding: 20px; }
.win-card { background: var(--card); border-radius: 28px; padding: 36px 28px; max-width: 440px; width: 100%; text-align: center; box-shadow: var(--shadow); animation: pop 0.3s ease; }
.win-icon { font-size: 52px; }
.win-card h2 { color: var(--pink-600); font-size: 26px; margin: 10px 0; }
.win-card p { color: var(--muted); margin: 0 0 22px; }
.win-actions { display: flex; gap: 12px; justify-content: center; }
.primary { background: var(--pink-500); color: #fff; border: none; border-radius: 999px; padding: 11px 20px; font-weight: 700; }
.secondary { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 11px 20px; font-weight: 700; }
@media (max-width: 560px) {
  .board { gap: 8px; }
  .emoji { font-size: 28px; }
  .name { font-size: 11px; }
  .main h1 { font-size: 24px; }
  .spacer { display: none; }
}
</style>
