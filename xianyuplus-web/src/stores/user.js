import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1)

  function init() {
    try {
      const raw = sessionStorage.getItem('user')
      if (raw) {
        const saved = JSON.parse(raw)
        if (saved.token) {
          token.value = saved.token
          userInfo.value = saved.userInfo || null
        }
      }
    } catch {}
  }

  function persist() {
    if (token.value) {
      sessionStorage.setItem('user', JSON.stringify({
        token: token.value,
        userInfo: userInfo.value
      }))
    } else {
      sessionStorage.removeItem('user')
    }
  }

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    userInfo.value = res.data.user
    persist()
  }

  async function register(form) {
    await request.post('/auth/register', form)
  }

  async function fetchInfo() {
    const res = await request.get('/auth/info')
    userInfo.value = res.data
    persist()
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    persist()
  }

  return { token, userInfo, isLoggedIn, isAdmin, init, login, register, fetchInfo, logout }
})
