<template>
  <div class="auth-page">
    <div class="auth-card card">
      <div class="auth-header">
        <div class="auth-icon">🎓</div>
        <h2 class="auth-title">欢迎回到闲鱼Plus</h2>
        <p class="auth-subtitle">登录你的账号，继续探索校园好物</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">👤 用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
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
            placeholder="请输入密码"
            :class="{ error: errors.password }"
          />
          <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
        </div>

        <button type="submit" class="btn-pill btn-pill-primary auth-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="auth-footer">
        还没有账号？<router-link to="/register" class="auth-link">立即注册 →</router-link>
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
const errors = reactive({ username: '', password: '' })

const form = reactive({
  username: '',
  password: ''
})

function validate() {
  errors.username = ''
  errors.password = ''
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    return false
  }
  if (!form.password) {
    errors.password = '请输入密码'
    return false
  }
  return true
}

async function handleLogin() {
  if (!validate()) return
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    Toast.success('登录成功')
    router.push('/')
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
  width: 400px;
  padding: 40px 36px;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
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
