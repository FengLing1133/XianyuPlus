<template>
  <div class="admin-users">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width: 250px;" @change="fetchData" />
      </div>
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '封禁' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 19) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="danger" @click="toggleStatus(row, 1)">封禁</el-button>
            <el-button v-else size="small" type="success" @click="toggleStatus(row, 0)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination" v-if="total > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/admin/users', { params })
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(user, status) {
  await request.put(`/admin/users/${user.id}/status`, null, { params: { status } })
  ElMessage.success(status === 1 ? '已封禁' : '已解封')
  fetchData()
}
</script>

<style scoped>
.admin-users { padding: 10px; }
.toolbar { margin-bottom: 15px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
