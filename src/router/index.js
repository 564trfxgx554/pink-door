import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'

// Simple auth guard: Home requires a token saved by the login page.
// The token is stored in sessionStorage so closing the tab logs her out.
const routes = [
  { path: '/', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true } },
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
