<template>
  <div class="quiz">
    <Petals />
    <header class="topbar">
      <button class="back" @click="leave">← 游戏列表</button>
      <div class="brand">💞 默契问答</div>
      <div class="spacer" />
    </header>

    <main class="main">
      <p v-if="connectionText" class="connection">{{ connectionText }}</p>
      <p v-if="errorText" class="error-toast">{{ errorText }}</p>

      <!-- 等待页 -->
      <section v-if="state === 'waiting'" class="panel">
        <h1 class="display">看看你们有多合拍</h1>
        <p class="lead">两个人都准备好，就开始今天的默契考验</p>

        <div class="players">
          <div
            v-for="member in members"
            :key="member.userId"
            class="player"
            :class="{ online: member.online, ready: isReady(member.userId) }"
          >
            <div class="avatar">{{ initial(member.nickname) }}</div>
            <strong>{{ member.nickname }}</strong>
            <span>
              {{ member.online
                ? (isReady(member.userId) ? '已准备好' : '已就位')
                : '等 TA 进来…' }}
            </span>
          </div>
        </div>

        <button class="primary" :disabled="myReady || members.length < 2 || !connected" @click="ready">
          {{ myReady ? '等对方准备好…' : (connected ? '我准备好了' : '正在连线…') }}
        </button>
        <p class="hint">房间 #{{ roomId }} · 同一空间的另一个人会自动进来</p>
      </section>

      <!-- 答题 / 等对方 -->
      <section v-else-if="isPlaying" class="panel">
        <div class="progress">
          <span>第 {{ roundNo }} / {{ totalRounds }} 题</span>
          <div class="dots">
            <i
              v-for="n in totalRounds"
              :key="n"
              :class="{ done: n < roundNo, current: n === roundNo }"
            />
          </div>
        </div>

        <div class="countdown" :class="{ danger: remaining <= 5 }">
          <span>{{ displaySeconds }}</span>
        </div>

        <h2 class="question">{{ question?.content }}</h2>

        <div class="options">
          <button
            v-for="(option, index) in question?.options"
            :key="index"
            class="option"
            :class="{ selected: myAnswer === index }"
            :disabled="state === 'waitingPartner'"
            @click="myAnswer = index"
          >
            <span class="option-tag">{{ optionLabels[index] }}</span>
            {{ option }}
          </button>
        </div>

        <button
          class="primary"
          :disabled="state === 'waitingPartner' || myAnswer === null"
          @click="submit"
        >
          {{ state === 'waitingPartner' ? '已提交，等 TA…' : '确定' }}
        </button>
      </section>

      <!-- 揭晓 -->
      <section v-else-if="state === 'reveal'" class="panel">
        <h1 class="display">{{ reveal?.matched ? '心有灵犀！' : '差一点点～' }}</h1>
        <p class="lead">{{ reveal?.content }}</p>

        <div class="answers">
          <div
            v-for="answer in reveal?.answers"
            :key="answer.userId"
            class="answer-card"
            :class="{ hit: reveal?.matched }"
          >
            <strong>{{ answer.nickname }}</strong>
            <span>{{ answerText(reveal, answer.optionIndex) }}</span>
          </div>
        </div>
        <p class="hint">下一题马上来…</p>
      </section>

      <!-- 结果 -->
      <section v-else-if="state === 'result'" class="panel">
        <h1 class="display">你们的默契度</h1>

        <div class="score-ring" :style="scoreStyle">
          <span>{{ result?.score }}%</span>
        </div>
        <p class="result-text">{{ result?.text }}</p>

        <div class="review">
          <div v-for="detail in result?.details" :key="detail.roundNo" class="review-item">
            <div class="review-head">
              <strong>第 {{ detail.roundNo }} 题</strong>
              <span :class="detail.matched ? 'hit-text' : 'miss-text'">
                {{ detail.matched ? '合拍' : '没合' }}
              </span>
            </div>
            <p>{{ detail.content }}</p>
            <div class="review-answers">
              <span v-for="answer in detail.answers" :key="answer.userId">
                {{ answer.nickname }}：{{ answerText(detail, answer.optionIndex) }}
              </span>
            </div>
          </div>
        </div>

        <div class="actions">
          <button class="primary" @click="restart">再来一局</button>
          <button class="secondary" @click="leave">回游戏列表</button>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'
import { createRoom, connectQuiz } from '../api/quiz'

const router = useRouter()

const state = ref('waiting')
const roomId = ref(null)
const members = ref([])
const readyIds = ref([])
const myReady = ref(false)
const roundNo = ref(0)
const totalRounds = ref(5)
const question = ref(null)
const myAnswer = ref(null)
const reveal = ref(null)
const result = ref(null)
const remaining = ref(0)
const connectionText = ref('')
const connected = ref(false)
const errorText = ref('')

const optionLabels = ['A', 'B', 'C', 'D']

const token = sessionStorage.getItem('pink-door-token')
let quiz = null
let ticker = null
let offset = 0
let deadlineAt = 0
let errorTimer = null

const isPlaying = computed(
  () => state.value === 'answering' || state.value === 'waitingPartner'
)
const displaySeconds = computed(() => Math.ceil(remaining.value))
const scoreStyle = computed(() => ({
  background: `conic-gradient(var(--pink-500) ${result.value?.score || 0}%, var(--pink-100) 0)`
}))

function initial(nickname) {
  return nickname ? nickname.slice(0, 1) : '?'
}

function isReady(userId) {
  return readyIds.value.includes(userId)
}

function answerText(detail, optionIndex) {
  if (!detail || optionIndex === null || optionIndex === undefined || optionIndex < 0) {
    return '没有回答'
  }
  return detail.options?.[optionIndex] ?? '没有回答'
}

function setError(message) {
  errorText.value = message
  clearTimeout(errorTimer)
  errorTimer = setTimeout(() => {
    errorText.value = ''
  }, 3000)
}

function tick() {
  remaining.value = Math.max(0, (deadlineAt - (Date.now() + offset)) / 1000)
  if (remaining.value <= 0) stopCountdown()
}

function startCountdown(deadline, serverNow) {
  offset = (serverNow || Date.now()) - Date.now()
  deadlineAt = deadline
  stopCountdown()
  tick()
  ticker = setInterval(tick, 100)
}

function stopCountdown() {
  if (ticker) clearInterval(ticker)
  ticker = null
}

function applySnapshot(data) {
  // 服务端 finished 在页面上就是结果页；否则刷新结果页会没有任何分支匹配。
  state.value = data.state === 'finished' ? 'result' : (data.state || 'waiting')
  if (data.room) {
    members.value = data.room.members || []
    totalRounds.value = data.room.totalRounds || totalRounds.value
  }
  roundNo.value = data.roundNo || data.room?.currentRound || 0
  if (data.question) {
    question.value = data.question
  }
  if (data.me) {
    myAnswer.value = data.me.answered && data.me.optionIndex >= 0
      ? data.me.optionIndex
      : null
  }
  if (data.deadlineAt) {
    startCountdown(data.deadlineAt, data.serverNow)
  } else {
    stopCountdown()
  }
  if (data.reveal) {
    reveal.value = data.reveal
  }
  if (data.result) {
    result.value = data.result
  }
}

function handleMessage(data) {
  switch (data.type) {
    case 'joined':
      members.value = data.room?.members || []
      break
    case 'readyState':
      readyIds.value = data.readyUserIds || []
      break
    case 'question':
      state.value = 'answering'
      roundNo.value = data.roundNo
      totalRounds.value = data.total
      question.value = { content: data.content, options: data.options }
      myAnswer.value = null
      reveal.value = null
      startCountdown(data.deadlineAt, data.serverNow)
      break
    case 'answerAccepted':
      state.value = 'waitingPartner'
      break
    case 'partnerAnswered':
      // 对方已提交，自己没答完时继续答题；自己已答时继续等揭晓。
      break
    case 'reveal':
      stopCountdown()
      state.value = 'reveal'
      reveal.value = data
      break
    case 'result':
      stopCountdown()
      state.value = 'result'
      result.value = data
      break
    case 'snapshot':
      applySnapshot(data)
      break
    case 'partnerOffline':
      setError('TA 掉线了，60 秒内回来还能继续。')
      break
    case 'partnerOnline':
      setError('TA 回来了，继续吧。')
      break
    case 'error':
      setError(data.message || '出了点小问题。')
      break
    default:
      break
  }
}

function ready() {
  if (myReady.value || !quiz) return
  myReady.value = true
  quiz.sendReady()
}

function submit() {
  if (state.value !== 'answering' || myAnswer.value === null || !quiz) return
  state.value = 'waitingPartner'
  quiz.sendAnswer(roundNo.value, myAnswer.value)
}

async function connect() {
  try {
    const room = await createRoom()
    roomId.value = room.roomId
    members.value = room.members || []
  } catch (error) {
    setError(error.message)
    return
  }

  quiz = connectQuiz({
    token,
    roomId: roomId.value,
    onMessage: handleMessage,
    onStatus: (status) => {
      if (status === 'connected') {
        connected.value = true
        connectionText.value = ''
      } else if (status === 'failed') {
        connected.value = false
        connectionText.value = '连接断开了，请刷新页面重试。'
      } else {
        // closed：正在重连中，准备按钮先禁用，等连上再恢复。
        connected.value = false
      }
    }
  })
}

async function restart() {
  quiz?.close()
  quiz = null
  state.value = 'waiting'
  members.value = []
  readyIds.value = []
  myReady.value = false
  question.value = null
  myAnswer.value = null
  reveal.value = null
  result.value = null
  stopCountdown()
  await connect()
}

function leave() {
  quiz?.close()
  router.push('/games')
}

if (!token) {
  router.push('/')
} else {
  connect()
}

onUnmounted(() => {
  quiz?.close()
  stopCountdown()
  clearTimeout(errorTimer)
})
</script>

<style scoped>
.quiz { min-height: 100vh; position: relative; overflow: hidden; }
.topbar { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: center; padding: 18px 28px; }
.back { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 8px 16px; font-weight: 700; }
.brand { font-size: 22px; font-weight: 800; color: var(--pink-600); }
.spacer { width: 92px; }
.main { position: relative; z-index: 2; max-width: 720px; margin: 0 auto; padding: 12px 20px 48px; text-align: center; }
.connection { color: var(--pink-600); background: var(--pink-100); border-radius: 999px; padding: 8px 14px; display: inline-block; }
.error-toast { color: #b03060; background: #ffe3ec; border-radius: 999px; padding: 8px 14px; display: inline-block; }

.panel { background: var(--card); border: 1px solid var(--pink-200); border-radius: 24px; box-shadow: var(--shadow); padding: 28px 24px; }
.display { color: var(--pink-600); font-size: 30px; margin: 4px 0 8px; }
.lead { color: var(--muted); margin: 0 0 22px; }

.players { display: flex; gap: 18px; justify-content: center; margin-bottom: 24px; }
.player { flex: 1; max-width: 200px; border: 1px dashed var(--pink-300); border-radius: 20px; padding: 18px 12px; color: var(--muted); }
.player.online { border-style: solid; color: var(--pink-600); }
.player.ready { background: var(--pink-100); border-color: var(--pink-400); }
.avatar { width: 58px; height: 58px; border-radius: 50%; margin: 0 auto 8px; display: grid; place-items: center; background: var(--pink-200); color: var(--pink-600); font-size: 24px; font-weight: 800; }
.player.online .avatar { background: linear-gradient(135deg, var(--pink-400), var(--pink-600)); color: #fff; }
.player strong { display: block; margin-bottom: 4px; }
.player span { font-size: 13px; }

.primary { background: linear-gradient(135deg, var(--pink-400), var(--pink-500)); color: #fff; border: none; border-radius: 999px; padding: 12px 26px; font-weight: 800; font-size: 16px; }
.primary:disabled { opacity: 0.55; }
.secondary { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 12px 26px; font-weight: 800; }
.hint { margin-top: 16px; color: var(--muted); font-size: 13.5px; }

.progress { display: flex; justify-content: space-between; align-items: center; color: var(--pink-600); font-weight: 700; margin-bottom: 16px; }
.dots { display: flex; gap: 6px; }
.dots i { width: 18px; height: 6px; border-radius: 999px; background: var(--pink-100); }
.dots i.done, .dots i.current { background: var(--pink-500); }

.countdown { width: 76px; height: 76px; border-radius: 50%; margin: 0 auto 18px; display: grid; place-items: center; border: 5px solid var(--pink-300); color: var(--pink-600); font-size: 28px; font-weight: 800; }
.countdown.danger { border-color: #e5486f; color: #e5486f; animation: pulse 0.8s ease infinite; }
.question { color: var(--pink-600); font-size: 26px; margin: 0 0 20px; line-height: 1.4; }

.options { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 22px; }
.option { display: flex; align-items: center; gap: 10px; text-align: left; background: var(--card); border: 2px solid var(--pink-200); border-radius: 16px; padding: 14px 16px; font-weight: 700; color: var(--pink-600); }
.option:disabled { opacity: 0.7; }
.option.selected { border-color: var(--pink-500); background: var(--pink-100); }
.option-tag { width: 26px; height: 26px; flex: none; border-radius: 50%; display: grid; place-items: center; background: var(--pink-100); font-size: 13px; }
.option.selected .option-tag { background: var(--pink-500); color: #fff; }

.answers { display: flex; gap: 14px; justify-content: center; margin: 20px 0; }
.answer-card { flex: 1; max-width: 220px; border: 2px solid var(--pink-200); border-radius: 18px; padding: 16px; display: grid; gap: 8px; }
.answer-card.hit { border-color: #4caf82; background: #eefaf3; }
.answer-card strong { color: var(--pink-600); }
.answer-card span { font-weight: 700; }

.score-ring { width: 150px; height: 150px; border-radius: 50%; margin: 18px auto; padding: 14px; display: grid; place-items: center; }
.score-ring span { width: 100%; height: 100%; border-radius: 50%; background: var(--card); display: grid; place-items: center; font-size: 34px; font-weight: 800; color: var(--pink-600); }
.result-text { color: var(--pink-600); font-weight: 700; margin: 0 0 22px; }

.review { text-align: left; display: grid; gap: 12px; margin-bottom: 22px; }
.review-item { background: #fff8fb; border: 1px solid var(--pink-100); border-radius: 16px; padding: 14px 16px; }
.review-head { display: flex; justify-content: space-between; color: var(--pink-600); }
.review-item p { margin: 6px 0; font-weight: 700; }
.review-answers { display: flex; gap: 14px; flex-wrap: wrap; color: var(--muted); font-size: 13.5px; }
.hit-text { color: #2e9e67; font-weight: 800; }
.miss-text { color: #b08a9a; font-weight: 800; }
.actions { display: flex; gap: 12px; justify-content: center; }

@keyframes pulse {
  50% { transform: scale(1.08); }
}

@media (max-width: 560px) {
  .options { grid-template-columns: 1fr; }
  .players { gap: 10px; }
  .display { font-size: 25px; }
  .question { font-size: 22px; }
  .answers { flex-direction: column; align-items: stretch; }
  .answer-card { max-width: none; }
  .spacer { display: none; }
}
</style>
