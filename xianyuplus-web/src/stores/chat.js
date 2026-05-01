import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useNotificationStore } from './notification'

export const useChatStore = defineStore('chat', () => {
  const ws = ref(null)
  const messages = ref([])
  const unreadCount = ref(0)
  const connected = ref(false)
  const currentUserId = ref(null)

  function connect(userId, token) {
    currentUserId.value = userId
    // Reuse existing connection
    if (ws.value && connected.value) {
      return
    }
    if (ws.value) {
      ws.value.close()
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const socket = new WebSocket(`${protocol}//${window.location.host}/ws/chat/${userId}?token=${token}`)
    ws.value = socket

    socket.onopen = () => {
      connected.value = true
    }

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data)

      // 处理通知推送
      if (data.type === 'notification') {
        const notificationStore = useNotificationStore()
        notificationStore.incrementUnread()
        notificationStore.fetchNotifications()
        return
      }

      const msg = data
      // Deduplicate: if message with same id already exists, skip
      if (msg.id && messages.value.some(m => m.id === msg.id)) return
      // If this is a server echo of our own message, replace the optimistic temp message
      if (String(msg.senderId) === String(currentUserId.value)) {
        const tempIdx = messages.value.findIndex(m =>
          typeof m.id === 'string' && m.id.startsWith('temp_') &&
          String(m.senderId) === String(currentUserId.value) && m.receiverId == msg.receiverId &&
          m.content === msg.content
        )
        if (tempIdx !== -1) {
          messages.value.splice(tempIdx, 1, msg)
          return
        }
      }
      messages.value.push(msg)
    }

    socket.onclose = () => {
      connected.value = false
    }

    socket.onerror = () => {
      connected.value = false
    }
  }

  function send(receiverId, productId, content, senderId) {
    if (!ws.value || !connected.value) {
      return false
    }
    // Optimistic update: add message to local list immediately
    const optimisticMsg = {
      id: 'temp_' + Date.now(),
      senderId,
      receiverId,
      productId,
      content,
      createdAt: new Date().toISOString()
    }
    messages.value.push(optimisticMsg)
    ws.value.send(JSON.stringify({ receiverId, productId, content }))
    return true
  }

  function setMessages(msgs) {
    messages.value = msgs
  }

  function disconnect() {
    if (ws.value) {
      ws.value.close()
      ws.value = null
      connected.value = false
    }
  }

  return { ws, messages, unreadCount, connected, connect, send, setMessages, disconnect }
})
