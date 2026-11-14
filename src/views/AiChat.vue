<template>
  <div class="ai-page">
    <Petals />

    <header class="topbar">
      <button class="ghost" type="button" @click="goHome">← 粉色小门</button>
      <div class="title">
        <span class="avatar">🌸</span>
        <div>
          <strong>小婷段</strong>
          <small>住在门后的 AI 小助手</small>
        </div>
      </div>
      <button class="new-chat" type="button" @click="startNewChat">＋ 新对话</button>
    </header>

    <main class="layout">
      <aside class="sidebar">
        <p class="sidebar-title">我们的聊天</p>
        <div
          v-for="conversation in conversations"
          :key="conversation.id"
          class="conversation"
          :class="{ active: conversation.id === currentId }"
        >
          <button type="button" class="conv-open" @click="openConversation(conversation.id)">
            <span class="conv-title">{{ conversation.title }}</span>
            <span class="conv-time">{{ shortTime(conversation.lastMessageAt) }}</span>
          </button>
          <button
            type="button"
            class="conv-delete"
            title="删除会话"
            :aria-label="`删除会话：${conversation.title}`"
            :disabled="sending || deletingId === conversation.id"
            @click.stop="askDelete(conversation)"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14M10 11v6M14 11v6" />
            </svg>
          </button>
        </div>
        <p v-if="!conversations.length" class="empty-hint">还没有聊天记录，先从一句话开始吧。</p>
      </aside>

      <section class="chat-panel">
        <div ref="scrollEl" class="messages">
          <div v-if="!messages.length" class="welcome">
            <div class="welcome-avatar">🌸</div>
            <h1 class="display">嗨，我是小婷段</h1>
            <p>想吐槽、想被抱抱、想约会点子，都可以跟我说。<br />今天想聊点什么？</p>
            <div class="quick-prompts">
              <button
                v-for="prompt in quickPrompts"
                :key="prompt"
                type="button"
                @click="sendQuick(prompt)"
              >
                {{ prompt }}
              </button>
            </div>
          </div>

          <div
            v-for="message in messages"
            :key="message.key"
            class="message"
            :class="message.role"
          >
            <div v-if="message.role === 'assistant'" class="bubble-avatar">🌸</div>
            <div class="bubble">
              <p>{{ message.content }}<span v-if="message.streaming" class="caret">▍</span></p>
            </div>
          </div>

          <p v-if="error" class="error" role="alert">{{ error }}</p>
        </div>

        <form class="composer" @submit.prevent="send">
          <textarea
            v-model="input"
            rows="1"
            maxlength="2000"
            placeholder="跟我说说今天的事…"
            :disabled="sending"
            @keydown.enter.exact.prevent="send"
          />
          <button type="submit" :disabled="sending || !input.trim()">
            {{ sending ? '小婷段在想…' : '发送 ♥' }}
          </button>
        </form>
        <p class="composer-note">小婷段是 AI 小助手，真正抱你的是他。</p>
      </section>
    </main>

    <div v-if="deleteTarget" class="modal-mask" @click.self="cancelDelete">
      <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
        <div class="modal-bow" aria-hidden="true">🎀</div>
        <h3 id="delete-title">删除这段聊天？</h3>
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
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'
import { deleteConversation, listConversations, listMessages, streamChat } from '../api/ai.js'

const router = useRouter()
const conversations = ref([])
const messages = ref([])
const input = ref('')
const currentId = ref(null)
const sending = ref(false)
const deletingId = ref(null)
const deleteTarget = ref(null)
const error = ref('')
const scrollEl = ref(null)

const quickPrompts = ['今天有点难过', '周末去哪玩', '夸夸我', '帮我写封小情书']

let keySeed = 0
let controller = null

function nextKey() {
  keySeed += 1
  return keySeed
}

function scrollToBottom() {
  nextTick(() => {
    if (scrollEl.value) {
      scrollEl.value.scrollTop = scrollEl.value.scrollHeight
    }
  })
}

watch(messages, scrollToBottom, { deep: true })

function showError(value) {
  error.value = value?.message || String(value)
}

function shortTime(value) {
  return value ? value.slice(5, 16) : ''
}

async function loadConversations() {
  try {
    conversations.value = await listConversations()
  } catch (value) {
    showError(value)
  }
}

async function openConversation(conversationId) {
  if (sending.value) return
  currentId.value = conversationId
  error.value = ''
  try {
    const history = await listMessages(conversationId)
    messages.value = history.map((message) => ({
      key: nextKey(),
      role: message.role,
      content: message.content,
      streaming: false
    }))
    scrollToBottom()
  } catch (value) {
    showError(value)
  }
}

function startNewChat() {
  if (sending.value) return
  currentId.value = null
  messages.value = []
  input.value = ''
  error.value = ''
}

function askDelete(conversation) {
  if (sending.value) return
  deleteTarget.value = conversation
}

function cancelDelete() {
  if (deletingId.value !== null) return
  deleteTarget.value = null
}

async function confirmDelete() {
  const target = deleteTarget.value
  if (!target || sending.value || deletingId.value !== null) return

  deletingId.value = target.id
  const wasCurrent = currentId.value === target.id
  try {
    await deleteConversation(target.id)
    conversations.value = conversations.value.filter((item) => item.id !== target.id)
    deleteTarget.value = null

    if (wasCurrent) {
      currentId.value = null
      messages.value = []
      error.value = ''
      if (conversations.value.length) {
        await openConversation(conversations.value[0].id)
      }
    }
  } catch (value) {
    showError(value)
  } finally {
    deletingId.value = null
  }
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return

  input.value = ''
  error.value = ''
  sending.value = true

  messages.value.push({
    key: nextKey(),
    role: 'user',
    content: text,
    streaming: false
  })
  messages.value.push({
    key: nextKey(),
    role: 'assistant',
    content: '',
    streaming: true
  })
  // 取数组里的响应式代理，后续增量修改才能触发视图更新。
  const assistant = messages.value[messages.value.length - 1]
  scrollToBottom()

  controller = new AbortController()
  try {
    await streamChat({
      conversationId: currentId.value,
      content: text,
      signal: controller.signal,
      onEvent(event) {
        if (event.type === 'meta') {
          currentId.value = event.conversationId
        } else if (event.type === 'delta') {
          assistant.content += event.content || ''
        } else if (event.type === 'done') {
          assistant.streaming = false
          assistant.id = event.messageId
        } else if (event.type === 'error') {
          assistant.streaming = false
          showError(event.message || '小婷段暂时没回上话，再试一次好吗？')
        }
      }
    })
    assistant.streaming = false
    await loadConversations()
  } catch (value) {
    assistant.streaming = false
    if (value.name !== 'AbortError') {
      showError(value)
    }
  } finally {
    sending.value = false
    controller = null
    scrollToBottom()
  }
}

function sendQuick(prompt) {
  if (sending.value) return
  input.value = prompt
  send()
}

function goHome() {
  router.push('/home')
}

onMounted(async () => {
  await loadConversations()
  if (conversations.value.length) {
    await openConversation(conversations.value[0].id)
  }
})

onBeforeUnmount(() => {
  if (controller) controller.abort()
})
</script>

<style scoped>
.ai-page {
  min-height: 100dvh;
  position: relative;
  display: flex;
  flex-direction: column;
}

.topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 24px;
}

.ghost,
.new-chat {
  border: 1px solid var(--card-border);
  background: var(--card);
  color: var(--ink);
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.ghost:hover,
.new-chat:hover {
  transform: translateY(-1px);
  border-color: var(--pink);
}

.new-chat {
  background: var(--pink);
  border-color: var(--pink);
  color: #fff;
}

.title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.avatar {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--pink-soft);
  font-size: 22px;
  flex: none;
}

.title strong {
  display: block;
  font-size: 17px;
  color: var(--ink);
}

.title small {
  display: block;
  color: var(--ink-soft);
  font-size: 12px;
  margin-top: 2px;
}

.layout {
  position: relative;
  z-index: 2;
  flex: 1;
  width: min(1120px, 100%);
  margin: 0 auto;
  padding: 0 20px 24px;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
  min-height: 0;
}

.sidebar {
  background: var(--card);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-card);
  padding: 16px;
  height: calc(100dvh - 120px);
  overflow-y: auto;
}

.sidebar-title {
  margin: 0 0 12px;
  color: var(--ink-soft);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
}

.conversation {
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 14px;
  padding: 10px 8px 10px 12px;
  margin-bottom: 6px;
  color: var(--ink);
  transition: background 0.18s ease, border-color 0.18s ease;
}

.conv-open {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
  text-align: left;
  background: transparent;
  border: none;
  padding: 0;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.conv-delete {
  flex: none;
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: var(--ink-soft);
  opacity: 0;
  transition: opacity 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.conv-delete svg {
  width: 15px;
  height: 15px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.conversation:hover .conv-delete,
.conversation.active .conv-delete,
.conv-delete:focus-visible {
  opacity: 1;
}

.conv-delete:hover:not(:disabled) {
  background: rgba(217, 54, 110, 0.1);
  color: var(--pink-deep);
}

.conv-delete:disabled {
  opacity: 0.4;
  cursor: wait;
}

@media (hover: none) {
  .conv-delete {
    opacity: 1;
  }
}

.conversation:hover,
.conversation.active {
  background: var(--pink-pale);
  border-color: var(--pink-100);
}

.conv-title {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 600;
}

.conv-time {
  color: var(--ink-soft);
  font-size: 11px;
}

.empty-hint {
  color: var(--ink-soft);
  font-size: 13px;
  line-height: 1.6;
}

.chat-panel {
  background: var(--card);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
  height: calc(100dvh - 120px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background-color: var(--card);
  /* 只给聊天消息区域加背景，保持照片比例并轻微压暗，保证文字可读。 */
  background-image:
    linear-gradient(rgba(255, 250, 253, 0.3), rgba(255, 250, 253, 0.3)),
    url('../assets/chat-bg.jpeg');
  background-size: cover;
  background-position: center 24%;
  background-repeat: no-repeat;
}

@media (prefers-color-scheme: dark) {
  .messages {
    background-image:
      linear-gradient(rgba(39, 18, 29, 0.55), rgba(39, 18, 29, 0.55)),
      url('../assets/chat-bg.jpeg');
  }
}

.welcome {
  margin: auto;
  text-align: center;
  max-width: 520px;
  padding: 20px;
}

/* 空状态文字直接压在照片上，用白色光晕保证可读，不额外挡住照片。 */
.welcome h1,
.welcome p {
  text-shadow:
    0 1px 14px rgba(255, 255, 255, 0.95),
    0 0 3px rgba(255, 255, 255, 0.9);
}

.welcome-avatar {
  width: 64px;
  height: 64px;
  margin: 0 auto 14px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--pink-soft);
  font-size: 32px;
}

.welcome h1 {
  margin: 0 0 10px;
  color: var(--pink-deep);
  font-size: 30px;
}

.welcome p {
  color: var(--ink-soft);
  line-height: 1.7;
  margin: 0 0 20px;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.quick-prompts button {
  border: 1px solid var(--card-border);
  background: var(--pink-pale);
  color: var(--ink);
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.quick-prompts button:hover {
  transform: translateY(-1px);
  border-color: var(--pink);
}

.message {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 82%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bubble-avatar {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--pink-soft);
  font-size: 16px;
  flex: none;
}

.bubble {
  background: var(--pink-pale);
  border: 1px solid var(--card-border);
  border-radius: 18px 18px 18px 6px;
  padding: 11px 14px;
}

.message.user .bubble {
  background: var(--pink);
  border-color: var(--pink);
  color: #fff;
  border-radius: 18px 18px 6px 18px;
}

.bubble p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.65;
  font-size: 15px;
}

.caret {
  display: inline-block;
  margin-left: 2px;
  animation: blink 0.9s steps(1) infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.error {
  color: #c0392b;
  background: #fdecea;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 13px;
  margin: 0;
}

.composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px 16px 8px;
  border-top: 1px solid var(--card-border);
  background: var(--pink-pale);
}

.composer textarea {
  flex: 1;
  resize: none;
  min-height: 44px;
  max-height: 140px;
  padding: 11px 14px;
  border: 1px solid var(--field-border);
  border-radius: 16px;
  background: var(--field);
  color: var(--ink);
  font: inherit;
  font-size: 15px;
  line-height: 1.5;
  outline: none;
}

.composer textarea:focus {
  border-color: var(--pink);
}

.composer button {
  height: 44px;
  padding: 0 18px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-500));
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(217, 54, 110, 0.24);
}

.composer button:disabled {
  opacity: 0.6;
  cursor: wait;
}

.composer-note {
  margin: 0;
  padding: 0 16px 12px;
  background: var(--pink-pale);
  color: var(--ink-soft);
  font-size: 11px;
  text-align: right;
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(91, 42, 63, 0.3);
  backdrop-filter: blur(4px);
}

.modal-card {
  width: min(380px, 100%);
  padding: 28px 24px 22px;
  border: 1px solid var(--card-border);
  border-radius: 24px;
  background: var(--card);
  box-shadow: var(--shadow);
  text-align: center;
  animation: pop 0.22s ease;
}

.modal-bow {
  margin-top: -50px;
  margin-bottom: 4px;
  font-size: 32px;
  animation: wiggle 2.5s ease-in-out infinite;
}

.modal-card h3 {
  margin: 0 0 8px;
  color: var(--pink-deep);
  font-size: 20px;
}

.modal-card p {
  margin: 0 0 20px;
  color: var(--ink-soft);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.modal-actions {
  display: flex;
  gap: 10px;
}

.modal-actions button {
  flex: 1;
  height: 42px;
  border-radius: 13px;
  font-size: 14px;
  font-weight: 700;
}

.modal-cancel {
  border: 1px solid var(--card-border);
  background: var(--pink-pale);
  color: var(--ink);
}

.modal-confirm {
  border: none;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-500));
  color: #fff;
  box-shadow: 0 8px 18px rgba(217, 54, 110, 0.24);
}

.modal-confirm:disabled {
  opacity: 0.6;
  cursor: wait;
}

@media (prefers-color-scheme: dark) {
  .welcome h1,
  .welcome p {
    text-shadow: 0 1px 14px rgba(39, 18, 29, 0.9);
  }
}

@media (max-width: 820px) {
  .topbar {
    padding: 12px 14px;
  }

  .title small {
    display: none;
  }

  .layout {
    grid-template-columns: 1fr;
    padding: 0 12px 16px;
  }

  .sidebar {
    display: none;
  }

  .chat-panel {
    height: calc(100dvh - 96px);
  }

  .messages {
    padding: 16px;
  }

  .message {
    max-width: 92%;
  }
}
</style>
