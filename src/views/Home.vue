<template>
  <div class="home">
    <Petals />
    <header class="topbar">
      <div class="brand">🌸 粉色小门</div>
      <div class="user">
        <span>欢迎回来，{{ nickname }} ♥</span>
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="main">
      <h1 class="display">欢迎回家，我的小公主</h1>
      <p class="lead">这扇粉色小门后面，藏着一个只属于我们两个人的地方</p>

      <div class="cards">
        <article class="card">
          <div class="card-icon">💬</div>
          <h2>AI 小助手</h2>
          <p>陪你聊天、写小情书、想约会点子，还记得我们的纪念日。</p>
          <span class="tag">即将上线 — Python + LangChain</span>
        </article>
        <article class="card">
          <div class="card-icon">🎮</div>
          <h2>情侣小游戏</h2>
          <p>照片记忆翻牌、默契问答、拼图，输了的人要请奶茶。</p>
          <span class="tag">即将上线</span>
        </article>
        <article class="card">
          <div class="card-icon">📸</div>
          <h2>我们的相册</h2>
          <p>你拍的每一张好看的照片，都按旅行、生日、日常好好收着。</p>
          <span class="tag">即将上线 — Java 后端</span>
        </article>
      </div>

      <p class="note">
        这是登录之后的主页，上面三个入口是下一步要做的页面，风格和登录页一样。
      </p>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'
import { logout } from '../api/auth.js'

const router = useRouter()
const nickname = ref(sessionStorage.getItem('pink-door-nickname') || '宝贝')

function handleLogout() {
  logout()
  router.push('/')
}
</script>

<style scoped>
.home { min-height: 100vh; position: relative; overflow: hidden; }
.topbar { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: center; padding: 18px 28px; }
.brand { font-size: 22px; font-weight: 800; color: var(--pink-600); }
.user { display: flex; gap: 14px; align-items: center; font-size: 15px; }
.user button { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 7px 14px; font-weight: 600; }
.main { position: relative; z-index: 2; max-width: 960px; margin: 0 auto; padding: 24px 20px 48px; text-align: center; }
.main h1 { font-size: 38px; color: var(--pink-600); margin: 8px 0; }
.lead { color: var(--muted); font-size: 18px; margin: 0 0 32px; }
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.card { background: var(--card); border: 1px solid var(--pink-200); border-radius: 22px; box-shadow: var(--shadow); padding: 28px 22px; text-align: left; }
.card-icon { font-size: 36px; }
.card h2 { margin: 10px 0 6px; color: var(--pink-600); font-size: 21px; }
.card p { color: var(--muted); line-height: 1.55; margin: 0 0 14px; }
.tag { display: inline-block; background: var(--pink-100); color: var(--pink-600); font-size: 12.5px; font-weight: 600; border-radius: 999px; padding: 5px 10px; }
.note { margin-top: 28px; color: var(--muted); font-size: 14px; }
@media (max-width: 760px) {
  .cards { grid-template-columns: 1fr; }
  .main h1 { font-size: 30px; }
  .user span { display: none; }
}
</style>
