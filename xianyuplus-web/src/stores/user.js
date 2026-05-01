import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1)

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    userInfo.value = res.data.user
  }

  async function register(form) {
    await request.post('/auth/register', form)
  }

  async function fetchInfo() {
    const res = await request.get('/auth/info')
    userInfo.value = res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
  }

  return { token, userInfo, isLoggedIn, isAdmin, login, register, fetchInfo, logout }
}, {
  persist: true
})
