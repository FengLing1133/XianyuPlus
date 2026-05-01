<template>
  <div class="notification-bell" @click.stop="togglePanel">
    <div class="bell-icon">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
      </svg>
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>

    <transition name="fade">
      <div v-if="showPanel" class="panel" @click.stop>
        <div class="panel-header">
          <span class="panel-title">通知</span>
          <button v-if="unreadCount > 0" class="mark-all-btn" @click="handleMarkAllRead">
            全部已读
          </button>
        </div>

        <div v-if="loading" class="panel-loading">加载中...</div>
        <div v-else-if="notifications.length === 0" class="panel-empty">暂无通知</div>
        <div v-else class="panel-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: item.isRead === 0 }"
            @click="handleClick(item)"
          >
            <div class="item-icon" :class="'type-' + item.type">
              {{ typeIcons[item.type] }}
            </div>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-desc">{{ item.content }}</div>
              <div class="item-time">{{ formatTime(item.createdAt) }}</div>
            </div>
            <button class="item-delete" @click.stop="handleDelete(item.id)">&times;</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { storeToRefs } from 'pinia'

const router = useRouter()
const store = useNotificationStore()
const { notifications, unreadCount, loading } = storeToRefs(store)

const showPanel = ref(false)

const typeIcons = {
  1: '📦',
  2: '💬',
  3: '❤️',
  4: '📢'
}

function togglePanel() {
  showPanel.value = !showPanel.value
  if (showPanel.value) {
    store.fetchNotifications()
  }
}

function closePanel() {
  showPanel.value = false
}

async function handleClick(item) {
  if (item.isRead === 0) {
    await store.markItemAsRead(item.id)
  }
  closePanel()

  // 根据通知类型跳转
  switch (item.type) {
    case 1: // 订单状态
      router.push('/orders')
      break
    case 2: // 新消息
      router.push('/chat')
      break
    case 3: // 商品相关
      if (item.relatedId) {
        router.push(`/product/${item.relatedId}`)
      }
      break
    case 4: // 系统通知（举报）
      if (item.title && item.title.includes('举报')) {
        router.push('/admin/reports')
      }
      break
  }
}

async function handleMarkAllRead() {
  await store.markAllItemsAsRead()
}

async function handleDelete(id) {
  await store.deleteItem(id)
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString('zh-CN')
}

// 点击外部关闭面板
function handleOutsideClick(e) {
  if (showPanel.value && !e.target.closest('.notification-bell')) {
    closePanel()
  }
}

onMounted(() => {
  store.fetchUnreadCount()
  document.addEventListener('click', handleOutsideClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  cursor: pointer;
}

.bell-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: background 0.2s;
}

.bell-icon:hover {
  background: rgba(0, 0, 0, 0.05);
}

.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: #ef4444;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 360px;
  max-height: 480px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
}

.mark-all-btn {
  font-size: 13px;
  color: var(--green-500);
  background: none;
  border: none;
  cursor: pointer;
}

.mark-all-btn:hover {
  text-decoration: underline;
}

.panel-loading,
.panel-empty {
  padding: 40px 16px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.panel-list {
  overflow-y: auto;
  flex: 1;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-item.unread:hover {
  background: #e8f2ff;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.item-icon.type-1 { background: #e8f4fd; }
.item-icon.type-2 { background: #e8f5e9; }
.item-icon.type-3 { background: #fce4ec; }
.item-icon.type-4 { background: #fff3e0; }

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.item-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #999;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.notification-item:hover .item-delete {
  opacity: 1;
}

.item-delete:hover {
  background: #fee2e2;
  color: #ef4444;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
