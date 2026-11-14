import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Games from '../views/Games.vue'
import MemoryGame from '../views/MemoryGame.vue'
import QuizGame from '../views/QuizGame.vue'
import AiChat from '../views/AiChat.vue'

// Simple auth guard: Home requires a token saved by the login page.
// The token is stored in sessionStorage so closing the tab logs her out.
const routes = [
  { path: '/', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true } },
  { path: '/games', name: 'Games', component: Games, meta: { requiresAuth: true } },
  { path: '/games/memory', name: 'MemoryGame', component: MemoryGame, meta: { requiresAuth: true } },
  { path: '/games/quiz', name: 'QuizGame', component: QuizGame, meta: { requiresAuth: true } },
  { path: '/ai', name: 'AiChat', component: AiChat, meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !sessionStorage.getItem('pink-door-token')) {
    return { name: 'Login' }
  }
  return true
})

export default router
