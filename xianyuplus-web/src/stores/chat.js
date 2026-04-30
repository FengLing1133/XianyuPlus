import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const ws = ref(null)
  const messages = ref([])
  const unreadCount = ref(0)
  const connected = ref(false)

  function connect(userId, token) {
    if (ws.value) {
      ws.value.close()
    }
    const socket = new WebSocket(`ws://localhost:8080/ws/chat/${userId}?token=${token}`)
    ws.value = socket

    socket.onopen = () => {
      connected.value = true
    }

    socket.onmessage = (event) => {
      const msg = JSON.parse(event.data)
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
    if (ws.value && connected.value) {
      ws.value.send(JSON.stringify({ receiverId, productId, content }))
    }
  }

  function disconnect() {
    if (ws.value) {
      ws.value.close()
      ws.value = null
      connected.value = false
    }
  }

  return { ws, messages, unreadCount, connected, connect, send, disconnect }
})
