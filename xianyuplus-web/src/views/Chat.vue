<template>
  <div class="chat-page">
    <h2 class="page-title">消息</h2>

    <div class="card">
      <div v-if="loading" style="padding: 40px; text-align: center; color: var(--text-secondary);">
        加载中...
      </div>
      <template v-else>
        <div v-for="conv in conversations" :key="conv.partnerId" class="conv-item" @click="$router.push(`/chat/${conv.partnerId}`)">
          <div class="conv-avatar">
            <img v-if="conv.partnerAvatar" :src="conv.partnerAvatar" class="avatar-img" />
            <span v-else class="avatar-default">头</span>
            <span v-if="conv.unread > 0" class="unread-dot">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
          </div>
          <div class="conv-body">
            <div class="conv-top">
              <span class="conv-name">{{ conv.partnerName }}</span>
              <span class="conv-time">{{ conv.lastTime?.substring(0, 16) }}</span>
            </div>
            <div class="conv-msg">{{ conv.lastMessage }}</div>
          </div>
        </div>
        <div v-if="conversations.length === 0" class="empty-state">
          <div class="empty-icon">-</div>
          <p>暂无消息</p>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const conversations = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await request.get('/message/conversations')
    conversations.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.chat-page {
  max-width: 700px;
  margin: 0 auto;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--border-lighter);
}
.conv-item:last-child { border-bottom: none; }
.conv-item:hover { background: #fafafa; }

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.avatar-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-default {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.unread-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  background: var(--price-red);
  color: #fff;
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  border-radius: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  font-weight: 600;
}

.conv-body { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-size: 15px; font-weight: 500; color: var(--text-primary); }
.conv-time { font-size: 12px; color: var(--text-muted); }
.conv-msg { font-size: 13px; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
