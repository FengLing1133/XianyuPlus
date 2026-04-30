<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-header">
        <div class="auth-icon">🎉</div>
        <h2 class="auth-title">加入闲鱼Plus</h2>
        <p class="auth-subtitle">注册账号，开启校园二手交易</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <label class="form-label">👤 用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="3-20位用户名"
            :class="{ error: errors.username }"
          />
          <span v-if="errors.username" class="form-error">{{ errors.username }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">🔒 密码</label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="6-20位密码"
            :class="{ error: errors.password }"
          />
          <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">🔒 确认密码</label>
          <input
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            placeholder="再次输入密码"
            :class="{ error: errors.confirmPassword }"
          />
          <span v-if="errors.confirmPassword" class="form-error">{{ errors.confirmPassword }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">😊 昵称 <span style="color:var(--text-muted);font-weight:400;">(选填)</span></label>
          <input
            v-model="form.nickname"
            type="text"
            class="form-input"
            placeholder="给自己起个好听的名字"
          />
        </div>

        <div class="form-group">
          <label class="form-label">📱 手机号 <span style="color:var(--text-muted);font-weight:400;">(选填)</span></label>
          <input
            v-model="form.phone"
            type="text"
            class="form-input"
            placeholder="方便联系"
          />
        </div>

        <button type="submit" class="btn-pill btn-pill-primary auth-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>注 册</span>
        </button>
      </form>

      <div class="auth-footer">
        已有账号？<router-link to="/login" class="auth-link">立即登录 →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Toast } from '@/utils/toast'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const errors = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

function validate() {
  errors.username = ''
  errors.password = ''
  errors.confirmPassword = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    return false
  }
  if (form.username.length < 3 || form.username.length > 20) {
    errors.username = '用户名长度3-20位'
    return false
  }
  if (!form.password) {
    errors.password = '请输入密码'
    return false
  }
  if (form.password.length < 6 || form.password.length > 20) {
    errors.password = '密码长度6-20位'
    return false
  }
  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次密码不一致'
    return false
  }
  return true
}

async function handleRegister() {
  if (!validate()) return
  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username,
      phone: form.phone
    })
    Toast.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 75vh;
}

.auth-card {
  width: 420px;
  padding: 36px;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.auth-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-form {
  margin-bottom: 20px;
}

.form-error {
  color: var(--price-red);
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.form-input.error {
  border-color: var(--price-red);
}

.auth-btn {
  width: 100%;
  padding: 12px;
  justify-content: center;
  font-size: 16px;
  margin-top: 8px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-link {
  color: var(--green-500);
  font-weight: 500;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
