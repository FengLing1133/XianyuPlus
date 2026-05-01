import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)

  async function fetchNotifications(page = 1, size = 20) {
    loading.value = true
    try {
      const res = await getNotifications(page, size)
      if (res.code === 200) {
        notifications.value = res.data.records || []
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      if (res.code === 200) {
        unreadCount.value = res.data || 0
      }
    } catch (e) {
      console.error('获取未读数量失败:', e)
    }
  }

  async function markItemAsRead(id) {
    await markAsRead(id)
    const item = notifications.value.find(n => n.id === id)
    if (item) {
      item.isRead = 1
    }
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  }

  async function markAllItemsAsRead() {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
  }

  async function deleteItem(id) {
    await deleteNotification(id)
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      const item = notifications.value[index]
      if (item.isRead === 0 && unreadCount.value > 0) {
        unreadCount.value--
      }
      notifications.value.splice(index, 1)
    }
  }

  function incrementUnread() {
    unreadCount.value++
  }

  return {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    fetchUnreadCount,
    markItemAsRead,
    markAllItemsAsRead,
    deleteItem,
    incrementUnread
  }
})
