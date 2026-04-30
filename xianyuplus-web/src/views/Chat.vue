<template>
  <div class="chat-page">
    <el-card>
      <template #header>消息</template>
      <div v-loading="loading">
        <div v-for="conv in conversations" :key="conv.partnerId" class="conv-item" @click="$router.push(`/chat/${conv.partnerId}`)">
          <el-badge :value="conv.unread" :hidden="conv.unread === 0">
            <el-avatar :size="48" :src="conv.partnerAvatar" />
          </el-badge>
          <div class="conv-info">
            <div class="conv-name">{{ conv.partnerName }}</div>
            <div class="conv-msg">{{ conv.lastMessage }}</div>
          </div>
          <div class="conv-time">{{ conv.lastTime?.substring(0, 16) }}</div>
        </div>
        <el-empty v-if="!loading && conversations.length === 0" description="暂无消息" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const conversations = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/message/conversations')
    conversations.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.chat-page { max-width: 700px; margin: 0 auto; }
.conv-item { display: flex; align-items: center; gap: 15px; padding: 15px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.conv-item:hover { background: #fafafa; }
.conv-info { flex: 1; }
.conv-name { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.conv-msg { font-size: 13px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 400px; }
.conv-time { font-size: 12px; color: #c0c4cc; }
</style>
