<template>
  <div class="profile-page">
    <el-card>
      <template #header>个人中心</template>
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userStore.userInfo?.avatar" />
          <el-upload :action="uploadUrl" :headers="headers" :show-file-list="false" :on-success="handleAvatarSuccess" accept="image/*">
            <el-button size="small" style="margin-top: 10px;">更换头像</el-button>
          </el-upload>
        </div>
        <div class="info-form">
          <el-form :model="form" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="userStore.userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>修改密码</template>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" style="max-width: 400px;">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- My products -->
    <el-card style="margin-top: 20px;">
      <template #header>我的发布</template>
      <div class="product-grid">
        <ProductCard v-for="item in myProducts" :key="item.id" :product="item" />
      </div>
      <el-empty v-if="myProducts.length === 0" description="暂无发布" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import ProductCard from '@/components/ProductCard.vue'

const userStore = useUserStore()
const pwdFormRef = ref(null)
const myProducts = ref([])

const uploadUrl = '/api/user/avatar'
const headers = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const form = reactive({
  nickname: userStore.userInfo?.nickname || '',
  phone: userStore.userInfo?.phone || ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ]
}

onMounted(async () => {
  const res = await request.get('/product/my')
  myProducts.value = res.data.records
})

async function saveProfile() {
  await request.put('/user/profile', form)
  userStore.fetchInfo()
  ElMessage.success('保存成功')
}

function handleAvatarSuccess(response) {
  userStore.fetchInfo()
  ElMessage.success('头像更新成功')
}

async function changePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  await request.put('/auth/password', pwdForm)
  ElMessage.success('密码修改成功')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
}
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
.profile-header { display: flex; gap: 40px; }
.avatar-section { display: flex; flex-direction: column; align-items: center; }
.info-form { flex: 1; }
.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
</style>
