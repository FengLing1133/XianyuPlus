import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) {
      return data
    }
    ElMessage.error(data.message || '请求失败')
    if (data.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
    return Promise.reject(new Error(data.message))
  },
  error => {
    const silent = error.config?.silent
    if (!silent) {
      if (error.response) {
        const { status, data } = error.response
        if (status === 401) {
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
        } else if (status === 403) {
          ElMessage.error('无权访问')
        } else {
          ElMessage.error(data?.message || `请求失败(${status})`)
        }
      } else {
        ElMessage.error('网络错误')
      }
    }
    return Promise.reject(error)
  }
)

export default request
