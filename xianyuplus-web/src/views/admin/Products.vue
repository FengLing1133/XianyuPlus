<template>
  <div class="admin-products">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索商品标题" clearable style="width: 250px;" @change="fetchData" />
        <el-select v-model="status" placeholder="状态筛选" clearable style="width: 130px; margin-left: 10px;" @change="fetchData">
          <el-option label="在售" :value="1" />
          <el-option label="已售出" :value="2" />
          <el-option label="已下架" :value="3" />
        </el-select>
      </div>
      <el-table :data="products" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 19) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该商品？" @confirm="deleteProduct(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
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

const products = ref([])
const loading = ref(false)
const keyword = ref('')
const status = ref(null)
const page = ref(1)
const total = ref(0)

function statusText(s) {
  const map = { 1: '在售', 2: '已售出', 3: '已下架' }
  return map[s] || ''
}

function statusType(s) {
  const map = { 1: 'success', 2: 'info', 3: 'warning' }
  return map[s] || 'info'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    if (status.value) params.status = status.value
    const res = await request.get('/admin/products', { params })
    products.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function deleteProduct(row) {
  await request.delete(`/admin/products/${row.id}`)
  ElMessage.success('已删除')
  fetchData()
}
</script>

<style scoped>
.admin-products { padding: 10px; }
.toolbar { margin-bottom: 15px; display: flex; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
