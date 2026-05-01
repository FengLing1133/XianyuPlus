import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const ws = ref(null)
  const messages = ref([])
  const unreadCount = ref(0)
  const connected = ref(false)

  function connect(userId, token) {
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
      const msg = JSON.parse(event.data)
      // Deduplicate: if message with same id already exists, skip
      if (msg.id && messages.value.some(m => m.id === msg.id)) return
      messages.value.push(msg)
    }

    socket.onclose = () => {
      connected.value = false
    }

    socket.onerror = () => {
      connected.value = false
    }
  }

  function send(receiverId, productId, content) {
    if (!ws.value || !connected.value) {
      return false
    }
    // Optimistic update: add message to local list immediately
    const optimisticMsg = {
      id: 'temp_' + Date.now(),
      senderId: null, // will be set by caller
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
