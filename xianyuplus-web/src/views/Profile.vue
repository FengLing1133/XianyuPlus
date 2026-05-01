<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <!-- Avatar & Info Card -->
    <div class="card profile-card">
      <div class="profile-header">
        <div class="avatar-area">
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="avatar-img" />
          <span v-else class="avatar-default"></span>
          <button class="btn-pill btn-upload" @click="triggerUpload">更换头像</button>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
        </div>
        <div class="info-area">
          <form @submit.prevent="saveProfile">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input class="form-input" :value="userStore.userInfo?.username" disabled style="background:#f9f9f9;" />
            </div>
            <div class="form-group">
              <label class="form-label">昵称</label>
              <input v-model="form.nickname" class="form-input" placeholder="你的昵称" />
            </div>
            <div class="form-group">
              <label class="form-label">手机号</label>
              <input v-model="form.phone" class="form-input" placeholder="你的手机号" />
            </div>
            <button type="submit" class="btn-pill btn-pill-primary">保存</button>
          </form>
        </div>
      </div>
    </div>

    <!-- Password Card -->
    <div class="card" style="margin-top: 20px; padding: 28px 32px;">
      <h3 class="section-title">修改密码</h3>
      <form @submit.prevent="changePassword" style="max-width: 400px;">
        <div class="form-group">
          <label class="form-label">原密码</label>
          <input v-model="pwdForm.oldPassword" type="password" class="form-input" placeholder="请输入原密码" />
          <span v-if="pwdErrors.oldPassword" class="form-error">{{ pwdErrors.oldPassword }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">新密码</label>
          <input v-model="pwdForm.newPassword" type="password" class="form-input" placeholder="6-20位新密码" />
          <span v-if="pwdErrors.newPassword" class="form-error">{{ pwdErrors.newPassword }}</span>
        </div>
        <button type="submit" class="btn-pill btn-pill-primary">修改密码</button>
      </form>
    </div>

    <!-- View History -->
    <router-link to="/history" class="profile-menu-item">
      <span class="menu-icon"></span>
      <span class="menu-text">浏览历史</span>
      <span class="menu-arrow">›</span>
    </router-link>

    <!-- My Products -->
    <div class="card" style="margin-top: 20px; padding: 28px 32px;">
      <h3 class="section-title">我的发布</h3>
      <div class="product-grid" v-if="myProducts.length > 0">
        <ProductCard v-for="item in myProducts" :key="item.id" :product="item" />
      </div>
      <div v-else class="empty-state" style="padding: 40px 0;">
        <div class="empty-icon"></div>
        <p>暂无发布</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { Toast } from '@/utils/toast'
import ProductCard from '@/components/ProductCard.vue'

const userStore = useUserStore()
const fileInput = ref(null)
const myProducts = ref([])

const form = reactive({
  nickname: userStore.userInfo?.nickname || '',
  phone: userStore.userInfo?.phone || ''
})

const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const pwdErrors = reactive({ oldPassword: '', newPassword: '' })

onMounted(async () => {
  try {
    const res = await request.get('/product/my')
    myProducts.value = res.data.records
  } catch {}
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    await request.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    await userStore.fetchInfo()
    Toast.success('头像更新成功')
  } catch {}
}

async function saveProfile() {
  await request.put('/user/profile', form)
  await userStore.fetchInfo()
  Toast.success('保存成功')
}

async function changePassword() {
  pwdErrors.oldPassword = ''
  pwdErrors.newPassword = ''
  if (!pwdForm.oldPassword) { pwdErrors.oldPassword = '请输入原密码'; return }
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6 || pwdForm.newPassword.length > 20) {
    pwdErrors.newPassword = '密码长度6-20位'; return
  }
  try {
    await request.put('/auth/password', pwdForm)
    Toast.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch {}
}
</script>

<style scoped>
.profile-page {
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-card {
  padding: 28px 32px;
}

.profile-header {
  display: flex;
  gap: 48px;
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-img {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-default {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.btn-upload {
  background: #f5f5f5;
  color: var(--text-secondary);
  padding: 6px 14px;
  font-size: 12px;
  border-radius: var(--radius-pill);
}
.btn-upload:hover { background: #eee; }

.info-area {
  flex: 1;
  max-width: 400px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.form-error {
  color: var(--price-red);
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    gap: 24px;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.profile-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-top: 20px;
  background: #fff;
  border-radius: 8px;
  text-decoration: none;
  color: #333;
  transition: background 0.2s;
}

.profile-menu-item:hover {
  background: #f8f9fa;
}

.menu-icon {
  font-size: 20px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
}

.menu-arrow {
  color: #999;
  font-size: 18px;
}
</style>
