import axios from 'axios'
import { Toast } from '@/utils/toast'
import router from '@/router'
import { pinia } from '@/pinia'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 从 Pinia 内存状态读 token，避免多标签页 localStorage 互相覆盖
function getToken() {
  try {
    const store = useUserStore(pinia)
    return store.token || ''
  } catch {
    // pinia 未初始化时的 fallback
    try {
      const raw = localStorage.getItem('user')
      if (raw) {
        const parsed = JSON.parse(raw)
        return parsed.token || ''
      }
    } catch {}
  }
  return ''
}

request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

function clearAuth() {
  try {
    const store = useUserStore(pinia)
    store.logout()
  } catch {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  router.push('/login')
}

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) {
      return data
    }
    Toast.error(data.message || '请求失败')
    if (data.code === 401) {
      clearAuth()
    }
    return Promise.reject(new Error(data.message))
  },
  error => {
    const silent = error.config?.silent
    if (!silent) {
      if (error.response) {
        const { status, data } = error.response
        if (status === 401) {
          Toast.error('登录已过期，请重新登录')
          clearAuth()
        } else if (status === 403) {
          Toast.error('无权访问')
        } else {
          Toast.error(data?.message || `请求失败(${status})`)
        }
      } else {
        Toast.error('网络错误')
      }
    }
    return Promise.reject(error)
  }
)

export default request
