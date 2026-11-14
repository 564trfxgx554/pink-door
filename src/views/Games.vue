<template>
  <div class="games">
    <Petals />
    <header class="topbar">
      <button class="back" @click="goHome">← 回主页</button>
      <div class="brand">🎮 情侣小游戏</div>
      <div class="spacer" />
    </header>

    <main class="main">
      <h1 class="display">挑一个，和他 / 她一起玩吧</h1>
      <p class="lead">三丽鸥的甜甜小世界，输了的人要请奶茶哦 💕</p>

      <div class="grid">
        <article
          v-for="game in games"
          :key="game.id"
          class="game-card"
          :class="{ playable: game.playable, locked: !game.playable }"
          @click="openGame(game)"
        >
          <div class="game-icon">{{ game.icon }}</div>
          <h2>{{ game.name }}</h2>
          <p>{{ game.desc }}</p>
          <span v-if="game.playable" class="tag play">可以玩啦 →</span>
          <span v-else class="tag soon">即将上线</span>
        </article>
      </div>

      <p class="note">照片翻牌先做好了，其他 7 个正在排队来哦，敬请期待 🎀</p>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Petals from '../components/Petals.vue'

const router = useRouter()

const games = [
  { id: 'memory', icon: '🃏', name: '照片记忆翻牌', desc: '用我们的照片做牌面，翻开两张一样的就配对成功，拼好你们的回忆。', playable: true, route: '/games/memory' },
  { id: 'quiz', icon: '💞', name: '默契问答', desc: '同一道题、同一个倒计时，各自盲答，一起揭晓，测测你们的默契度。', playable: true, route: '/games/quiz' },
  { id: 'wheel', icon: '🎡', name: '约会大转盘', desc: '吃饭、看电影、去公园、宅家做饭……转到哪个就去哪个。', playable: false },
  { id: 'match3', icon: '💖', name: '爱心消消乐', desc: '爱心、玫瑰、樱花三连消，轻松解压，无聊的时候也能玩。', playable: false },
  { id: 'truth', icon: '🎴', name: '真心话大冒险', desc: '抽一张卡，说一件最想谢谢对方的事，或者发一张现在的自拍。', playable: false },
  { id: 'puzzle', icon: '🧩', name: '拼图', desc: '选一张合照切成小块，拖着拼回去，拼好才能解锁下一张。', playable: false },
  { id: 'catch', icon: '🌸', name: '双人接爱心', desc: '爱心和玫瑰往下掉，两个人各控制一边接，合作才能得高分。', playable: false },
  { id: 'flower', icon: '🌷', name: '一起种花', desc: '每天浇一次水，花就长大一点，开花解锁一张照片或一句情话。', playable: false }
]

function goHome() {
  router.push('/home')
}

function openGame(game) {
  if (game.playable && game.route) router.push(game.route)
}
</script>

<style scoped>
.games { min-height: 100vh; position: relative; overflow: hidden; }
.topbar { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: center; padding: 18px 28px; }
.back { background: var(--card); border: 1px solid var(--pink-300); color: var(--pink-600); border-radius: 999px; padding: 8px 16px; font-weight: 700; }
.brand { font-size: 22px; font-weight: 800; color: var(--pink-600); }
.spacer { width: 92px; }
.main { position: relative; z-index: 2; max-width: 960px; margin: 0 auto; padding: 12px 20px 48px; text-align: center; }
.main h1 { font-size: 34px; color: var(--pink-600); margin: 8px 0; }
.lead { color: var(--muted); font-size: 17px; margin: 0 0 28px; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; text-align: left; }
.game-card { background: var(--card); border: 1px solid var(--pink-200); border-radius: 24px; box-shadow: var(--shadow); padding: 24px 20px; }
.game-card.playable { cursor: pointer; border-color: var(--pink-300); }
.game-card.playable:hover { transform: translateY(-3px); }
.game-card.locked { opacity: 0.75; }
.game-icon { font-size: 38px; }
.game-card h2 { margin: 10px 0 6px; color: var(--pink-600); font-size: 20px; }
.game-card p { color: var(--muted); line-height: 1.55; margin: 0 0 14px; font-size: 14.5px; min-height: 44px; }
.tag { display: inline-block; font-size: 12.5px; font-weight: 700; border-radius: 999px; padding: 5px 11px; }
.tag.play { background: var(--pink-500); color: #fff; }
.tag.soon { background: var(--pink-100); color: var(--pink-600); }
.note { margin-top: 28px; color: var(--muted); font-size: 14px; }
@media (max-width: 760px) {
  .grid { grid-template-columns: 1fr; }
  .main h1 { font-size: 28px; }
  .spacer { display: none; }
}
</style>
