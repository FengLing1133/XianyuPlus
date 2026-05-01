import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from './pinia'
import { useUserStore } from './stores/user'
import './styles/global.css'

// 初始化用户 store，从 sessionStorage 恢复登录状态
const userStore = useUserStore(pinia)
userStore.init()

const app = createApp(App)
app.use(pinia)
app.use(router)
app.mount('#app')
