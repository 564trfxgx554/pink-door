import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'
// Self-hosted fonts via Fontsource (bundled from node_modules at build
// time, so the repo stays text-only and the page needs no Google Fonts,
// which is blocked in mainland China).
import '@fontsource/quicksand/500.css'
import '@fontsource/quicksand/600.css'
import '@fontsource/quicksand/700.css'
import '@fontsource/zcool-kuaile/400.css'

createApp(App).use(router).mount('#app')
