import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import AiChat from '../views/AiChat.vue'

// 登录守卫：首页和 AI 聊天页需要登录页保存的 token。
// token 存在 sessionStorage 中，关闭标签页即自动退出登录。
const routes = [
  { path: '/', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true } },
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
