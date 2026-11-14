<template>
  <div class="login-page">
    <Petals />

    <main class="shell">
      <!-- 左侧：欢迎 + 插画 + 进来后有什么 -->
      <section class="intro">
        <span class="tag">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21s-7.5-4.9-10-9.3C.4 8.6 2.3 5 5.7 5c2 0 3.4 1.1 4.3 2.4h4c.9-1.3 2.3-2.4 4.3-2.4 3.4 0 5.3 3.6 3.7 6.7C19.5 16.1 12 21 12 21Z" transform="scale(.92) translate(1,0)" /></svg>
          只为你一个人开放的小世界
        </span>
        <h1 class="display">欢迎回家，<br /><span class="accent">我的小公主</span></h1>
        <p class="lead">这扇粉色小门后面，藏着一个只属于我们两个人的地方</p>

        <div class="hero-frame">
          <CuteIllustration />
          <div class="hero-badge">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
            今天也超级喜欢你
          </div>
        </div>

      </section>

      <!-- 右侧：登录卡 -->
      <section>
        <div ref="cardEl" class="card" :class="{ shake }">
          <!-- 蝴蝶结 -->
          <svg class="bow" viewBox="0 0 100 64" aria-hidden="true">
            <path d="M50 32 C38 8, 8 2, 6 22 C4 40, 30 44, 50 32 Z" fill="#ff8fb5" />
            <path d="M50 32 C62 8, 92 2, 94 22 C96 40, 70 44, 50 32 Z" fill="#ff8fb5" />
            <path d="M50 32 C40 16, 16 12, 15 24 C14 34, 36 38, 50 32 Z" fill="#ffabc7" />
            <path d="M50 32 C60 16, 84 12, 85 24 C86 34, 64 38, 50 32 Z" fill="#ffabc7" />
            <circle cx="50" cy="32" r="9" fill="#f4538a" />
            <circle cx="47" cy="29" r="3" fill="#ffabc7" />
          </svg>

          <div v-if="!success">
            <h2 class="display">推开这扇粉色小门</h2>
            <p class="sub">输入你的昵称和我们的专属暗号，<br />就能回到我们的小屋啦。</p>

            <form novalidate @submit.prevent="handleLogin">
              <div class="field">
                <label for="username"><span class="lbl-ic"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z" /></svg></span>你的昵称</label>
                <div class="input-wrap">
                  <input
                    id="username"
                    ref="usernameEl"
                    v-model="nickname"
                    type="text"
                    autocomplete="username"
                    placeholder="比如：宝贝、小仙女"
                    maxlength="20"
                    :disabled="loading"
                    @input="hideError"
                  />
                </div>
              </div>

              <div class="field">
                <label for="password"><span class="lbl-ic"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2a5 5 0 0 0-5 5v3H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-1V7a5 5 0 0 0-5-5Zm-3 8V7a3 3 0 1 1 6 0v3Z" /></svg></span>我们的暗号</label>
                <div class="input-wrap">
                  <input
                    id="password"
                    ref="passwordEl"
                    v-model="password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    placeholder="只有我们两个人知道的那句"
                    maxlength="32"
                    :disabled="loading"
                    @input="hideError"
                  />
                  <button class="eye" type="button" :aria-label="showPassword ? '隐藏暗号' : '显示暗号'" @click="togglePassword">
                    <svg v-if="!showPassword" viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" /></svg>
                    <svg v-else viewBox="0 0 24 24" aria-hidden="true"><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c6.5 0 10 8 10 8a18.5 18.5 0 0 1-2.16 3.19M6.61 6.61A13.53 13.53 0 0 0 2 12s3.5 8 10 8a9.74 9.74 0 0 0 5.39-1.61" /><line x1="2" y1="2" x2="22" y2="22" /><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" /></svg>
                  </button>
                </div>
              </div>

              <div v-show="error" class="error show" role="alert">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
                <span>{{ error }}</span>
              </div>

              <button class="login-btn" :class="{ loading }" type="submit" :disabled="loading">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
                <span>{{ loading ? '正在为你开门…' : '带着爱心进去' }}</span>
              </button>

              <div class="row-between">
                <span v-if="USE_MOCK_AUTH" class="demo-hint">前端演示版：随便填一组就能体验进门效果</span>
                <button class="link-btn" type="button" @click="forgotCode">暗号忘了？</button>
              </div>
            </form>

            <p class="quote">每一次登录，都是奔向你的一次小跑。<br />门后没有别人，<b>只有我们</b>。</p>
          </div>

          <!-- 登录成功 -->
          <div v-else class="success show">
            <span
              v-for="heart in burstHearts"
              :key="heart.id"
              class="burst-heart"
              :style="{
                width: heart.size + 'px',
                height: heart.size + 'px',
                '--dx': heart.dx + 'px',
                '--dy': heart.dy + 'px',
                '--rot': heart.rot + 'deg'
              }"
            >
              <svg viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
            </span>
            <div class="big-heart">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
            </div>
            <h3 class="display">欢迎回来，{{ nickname }}</h3>
            <p>门开啦。AI 小助手已经在里面等你，<br />相册和小游戏也都准备好了。</p>
            <button class="login-btn" type="button" style="max-width: 280px; margin: 0 auto;" @click="enterHome">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
              进入我们的首页
            </button>
            <div style="margin-top: 16px;">
              <button class="ghost-btn" type="button" @click="backToLogin">先退出，再抱一下</button>
            </div>
          </div>
        </div>

      </section>

    </main>

    <!-- 底部：三个功能横排铺满整行，不再挤在左边 -->
    <ul class="features">
      <li>
        <span class="ic"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" /></svg></span>
        <div><b>AI 小助手</b><span>陪你聊天、写小情书、想约会点子，还记得我们的纪念日。</span></div>
      </li>
      <li>
        <span class="ic"><svg viewBox="0 0 24 24" aria-hidden="true"><line x1="6" y1="11" x2="10" y2="11" /><line x1="8" y1="9" x2="8" y2="13" /><line x1="15" y1="12" x2="15.01" y2="12" /><line x1="18" y1="10" x2="18.01" y2="10" /><path d="M17.32 5H6.68a4.68 4.68 0 0 0-4.583 5.628l.933 5.6A3 3 0 0 0 5.99 18.5c.87 0 1.71-.35 2.32-.97L9.5 16.5h5l1.19 1.03c.61.62 1.45.97 2.32.97a3 3 0 0 0 2.96-2.272l.933-5.6A4.68 4.68 0 0 0 17.32 5Z" /></svg></span>
        <div><b>情侣小游戏</b><span>照片记忆翻牌、默契问答、拼图，输了的人要请奶茶。</span></div>
      </li>
      <li>
        <span class="ic"><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="3" /><circle cx="9" cy="9" r="2" /><path d="m21 15-3.1-3.1a2 2 0 0 0-2.8 0L6 21" /></svg></span>
        <div><b>我们的相册</b><span>你拍的每一张好看的照片，都按旅行、生日、日常好好收着。</span></div>
      </li>
    </ul>

    <p class="page-foot">
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" /></svg>
      用代码和真心，一起为你做的 · 粉色小屋
    </p>

    <div class="toast" :class="{ show: toastVisible }" role="status" aria-live="polite">{{ toastMessage }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'
import CuteIllustration from '../components/CuteIllustration.vue'
import { login, USE_MOCK_AUTH } from '../api/auth.js'

const router = useRouter()
const nickname = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const success = ref(false)
const error = ref('')
const shake = ref(false)
const toastMessage = ref('')
const toastVisible = ref(false)
const burstHearts = ref([])
const cardEl = ref(null)
const usernameEl = ref(null)
const passwordEl = ref(null)

let toastTimer = null

function showToast(msg) {
  toastMessage.value = msg
  toastVisible.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toastVisible.value = false }, 3400)
}

function showError(msg) {
  error.value = msg
  // Restart the shake animation, same as the original page.
  shake.value = false
  void cardEl.value?.offsetWidth
  shake.value = true
  setTimeout(() => { shake.value = false }, 500)
}

function hideError() {
  error.value = ''
}

function togglePassword() {
  showPassword.value = !showPassword.value
  passwordEl.value?.focus()
}

function forgotCode() {
  showToast('暗号忘了也没关系，去抱一下那个最爱你的人，他会悄悄告诉你的。')
}

// 14 hearts bursting out from the big heart, same as the original page.
function burst() {
  burstHearts.value = Array.from({ length: 14 }, (_, i) => {
    const ang = (Math.PI * 2 * i) / 14 + Math.random() * 0.4
    const dist = 80 + Math.random() * 90
    return {
      id: i,
      size: 12 + Math.random() * 16,
      dx: Math.cos(ang) * dist,
      dy: Math.sin(ang) * dist - 30,
      rot: Math.random() * 90 - 45
    }
  })
  setTimeout(() => { burstHearts.value = [] }, 1100)
}

async function handleLogin() {
  const name = nickname.value.trim()
  const pwd = password.value

  // Same validation messages as the original page.
  if (!name) { showError('还没有告诉门，你是谁呀？先填一下昵称。'); usernameEl.value?.focus(); return }
  if (!pwd) { showError('暗号是空的，门不敢开。悄悄输进来试试。'); passwordEl.value?.focus(); return }
  if (pwd.length < 4) { showError('暗号太短啦，至少 4 个字，门才认得出来是你。'); passwordEl.value?.focus(); return }

  hideError()
  loading.value = true
  try {
    const result = await login(name, pwd)
    // The password is never stored — only the token and nickname.
    sessionStorage.setItem('pink-door-token', result.token)
    sessionStorage.setItem('pink-door-nickname', result.nickname)
    nickname.value = result.nickname
    success.value = true
    burst()
  } catch (err) {
    // 401 from the backend comes back as “暗号不对哦”.
    showError(err.message || '暗号不对哦')
  } finally {
    loading.value = false
  }
}

function enterHome() {
  router.push('/home')
}

function backToLogin() {
  success.value = false
  password.value = ''
  sessionStorage.removeItem('pink-door-token')
  sessionStorage.removeItem('pink-door-nickname')
  usernameEl.value?.focus()
}
</script>

<style scoped>
/* Layout and card styles ported 1:1 from the original login page. */
.login-page { min-height: 100dvh; }

.shell {
  position: relative;
  z-index: 1;
  max-width: 1060px;
  margin: 0 auto;
  padding: 36px 20px 8px;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 44px;
  align-items: center;
}

/* ---------- left intro ---------- */
.tag {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: var(--card);
  border: 1.5px solid var(--card-border);
  color: var(--pink-deep);
  font-weight: 700;
  font-size: 13px;
  padding: 7px 14px;
  border-radius: 999px;
}
.tag svg { width: 14px; height: 14px; fill: var(--pink); }
h1 {
  font-size: clamp(38px, 5.2vw, 58px);
  line-height: 1.22;
  margin: 18px 0 14px;
  letter-spacing: 0.01em;
}
h1 .accent { color: var(--pink-deep); position: relative; white-space: nowrap; }
.lead {
  font-size: 16.5px;
  line-height: 1.85;
  color: var(--ink-soft);
  margin: 0 0 18px;
  max-width: 30em;
  font-weight: 600;
}

.hero-frame { position: relative; max-width: 372px; }
.hero-badge {
  position: absolute;
  right: -10px;
  bottom: 18px;
  background: var(--card);
  border: 1.5px solid var(--card-border);
  border-radius: 16px;
  padding: 9px 14px;
  box-shadow: 0 10px 24px rgba(217, 54, 110, 0.16);
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 8px;
}
.hero-badge svg { width: 16px; height: 16px; fill: var(--pink); flex: none; }

.features {
  position: relative;
  z-index: 1;
  list-style: none;
  margin: 20px auto 0;
  padding: 0 20px;
  max-width: 1060px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.features li {
  display: flex;
  gap: 13px;
  align-items: flex-start;
  background: color-mix(in srgb, var(--card) 72%, transparent);
  border: 1.5px solid var(--card-border);
  border-radius: 18px;
  padding: 13px 16px;
}
.features .ic {
  width: 38px;
  height: 38px;
  flex: none;
  border-radius: 12px;
  background: var(--pink-soft);
  display: grid;
  place-items: center;
}
.features .ic svg {
  width: 20px;
  height: 20px;
  stroke: var(--pink-deep);
  fill: none;
  stroke-width: 1.9;
  stroke-linecap: round;
  stroke-linejoin: round;
}
.features b { display: block; font-size: 15px; margin-bottom: 2px; }
.features span span { font-size: 13.5px; color: var(--ink-soft); line-height: 1.55; font-weight: 600; }

/* ---------- login card ---------- */
.card {
  position: relative;
  background: var(--card);
  border: 1.5px solid var(--card-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
  padding: 52px 34px 46px;
}
.card.shake { animation: shake 0.45s ease; }
.bow {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  width: 64px;
  height: auto;
  filter: drop-shadow(0 6px 10px rgba(217, 54, 110, 0.25));
}
.card h2 { font-size: 27px; margin: 10px 0 6px; text-align: center; }
.card .sub {
  text-align: center;
  color: var(--ink-soft);
  font-size: 14.5px;
  font-weight: 600;
  margin: 0 0 26px;
  line-height: 1.7;
}

.field { margin-bottom: 18px; }
.field label { display: block; font-size: 14px; font-weight: 700; margin-bottom: 8px; }
.field label .lbl-ic {
  display: inline-grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  background: var(--pink-soft);
  vertical-align: -5px;
  margin-right: 6px;
}
.field label .lbl-ic svg { width: 12px; height: 12px; fill: var(--pink-deep); }
.input-wrap { position: relative; }
.input-wrap input {
  width: 100%;
  font: inherit;
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
  background: var(--field);
  border: 2px solid var(--field-border);
  border-radius: 16px;
  padding: 14px 48px 14px 16px;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}
.input-wrap input::placeholder { color: var(--ink-soft); opacity: 0.65; font-weight: 500; }
.input-wrap input:focus { border-color: var(--pink); box-shadow: 0 0 0 4px var(--pink-soft); }
.eye {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  background: transparent;
  display: grid;
  place-items: center;
  color: var(--ink-soft);
}
.eye:hover { background: var(--pink-soft); color: var(--pink-deep); }
.eye svg {
  width: 20px;
  height: 20px;
  stroke: currentColor;
  fill: none;
  stroke-width: 1.9;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.error {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: var(--pink-pale);
  border: 1.5px dashed var(--pink);
  color: var(--pink-deep);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.6;
  border-radius: 14px;
  padding: 10px 14px;
  margin: 2px 0 16px;
}
.error svg { width: 16px; height: 16px; flex: none; margin-top: 3px; fill: var(--pink); }

.login-btn {
  width: 100%;
  border: none;
  cursor: pointer;
  font: inherit;
  font-family: "ZCOOL KuaiLe", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 20px;
  letter-spacing: 0.06em;
  color: #fff;
  background: linear-gradient(135deg, #ff7aa8, var(--pink-deep));
  border-radius: 999px;
  padding: 15px 20px;
  box-shadow: 0 12px 26px rgba(217, 54, 110, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}
.login-btn:hover { transform: translateY(-2px); box-shadow: 0 16px 30px rgba(217, 54, 110, 0.4); }
.login-btn:active { transform: translateY(0) scale(0.99); }
.login-btn:disabled { opacity: 0.75; cursor: wait; transform: none; }
.login-btn svg { width: 19px; height: 19px; fill: #fff; }
.login-btn.loading svg { animation: pulse 0.7s ease-in-out infinite alternate; }

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.demo-hint { font-size: 13px; color: var(--ink-soft); font-weight: 600; }
.link-btn {
  border: none;
  background: none;
  font: inherit;
  cursor: pointer;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--pink-deep);
  text-decoration: underline;
  text-underline-offset: 3px;
  padding: 4px 2px;
}
.quote {
  margin: 24px 0 0;
  padding-top: 20px;
  border-top: 1.5px dashed var(--card-border);
  text-align: center;
  font-size: 14px;
  color: var(--ink-soft);
  font-weight: 600;
  line-height: 1.8;
}
.quote b { color: var(--pink-deep); }

/* success state */
.success { text-align: center; padding: 12px 2px 6px; position: relative; }
.success .big-heart { width: 76px; height: 76px; margin: 4px auto 14px; position: relative; }
.success .big-heart svg {
  width: 100%;
  height: 100%;
  fill: var(--pink);
  animation: beat 1s ease-in-out infinite;
  transform-origin: center;
}
.success h3 { font-size: 26px; margin: 0 0 8px; }
.success p {
  color: var(--ink-soft);
  font-weight: 600;
  line-height: 1.8;
  font-size: 15px;
  margin: 0 auto 22px;
  max-width: 24em;
}
.ghost-btn {
  border: 2px solid var(--pink);
  background: transparent;
  color: var(--pink-deep);
  font: inherit;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  border-radius: 999px;
  padding: 11px 22px;
}
.ghost-btn:hover { background: var(--pink-soft); }
.burst-heart { position: absolute; pointer-events: none; animation: burst 1s ease-out forwards; left: 50%; top: 56px; }
.burst-heart svg { fill: var(--pink); display: block; width: 100%; height: 100%; }

.page-foot {
  position: relative;
  z-index: 1;
  text-align: center;
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 600;
  padding: 22px 20px 28px;
  margin: 0;
}
.page-foot svg { width: 12px; height: 12px; fill: var(--pink); vertical-align: -1px; }

.toast {
  position: fixed;
  left: 50%;
  bottom: 26px;
  transform: translate(-50%, 90px);
  background: var(--ink);
  color: var(--bg);
  font-size: 14.5px;
  font-weight: 700;
  line-height: 1.6;
  padding: 13px 20px;
  border-radius: 16px;
  max-width: min(92vw, 460px);
  text-align: center;
  box-shadow: 0 16px 34px rgba(0, 0, 0, 0.25);
  z-index: 20;
  transition: transform 0.3s ease;
  pointer-events: none;
}
.toast.show { transform: translate(-50%, 0); }

@media (max-width: 860px) {
  .shell { grid-template-columns: 1fr; gap: 30px; padding: 28px 16px 8px; min-height: 0; }
  .intro { text-align: center; }
  .lead { margin-left: auto; margin-right: auto; }
  .hero-frame { margin: 0 auto; max-width: 360px; }
  .features { grid-template-columns: 1fr; padding: 0 16px; text-align: left; }
  .card { padding: 34px 22px 26px; }
  h1 { font-size: 36px; }
}
</style>
