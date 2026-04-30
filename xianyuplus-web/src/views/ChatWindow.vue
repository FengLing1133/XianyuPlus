<template>
  <div class="chat-window">
    <div class="chat-card card">
      <!-- Header -->
      <div class="chat-top">
        <button class="back-btn" @click="$router.push('/chat')">← 返回</button>
        <span class="partner-name">{{ partnerName || '聊天' }}</span>
        <span></span>
      </div>

      <!-- Messages -->
      <div class="msg-list" ref="msgListRef">
        <div v-for="msg in messages" :key="msg.id" :class="['msg-item', msg.senderId === currentUserId ? 'msg-me' : 'msg-other']">
          <div class="msg-bubble">
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-time">{{ msg.createdAt?.substring(0, 16) }}</div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="empty-state" style="padding: 40px 0;">
          <div class="empty-icon">💭</div>
          <p>暂无消息，发送第一条吧</p>
        </div>
      </div>

      <!-- Input -->
      <div class="input-area">
        <textarea
          v-model="inputText"
          class="msg-input"
          rows="2"
          placeholder="输入消息..."
          @keyup.enter.exact="sendMsg"
        ></textarea>
        <button class="btn-pill btn-pill-primary send-btn" @click="sendMsg" :disabled="!inputText.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'

const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()
const msgListRef = ref(null)
const inputText = ref('')
const messages = ref([])
const partnerName = ref('')
const currentUserId = userStore.userInfo?.id

onMounted(async () => {
  const partnerId = route.params.userId

  try {
    const res = await request.get(`/message/${partnerId}`)
    messages.value = res.data
  } catch {}

  try {
    const userRes = await request.get(`/user/${partnerId}`)
    partnerName.value = userRes.data.nickname
  } catch {}

  chatStore.connect(currentUserId, userStore.token)
  chatStore.messages = messages.value

  scrollToBottom()
})

onUnmounted(() => {
  chatStore.disconnect()
})

function sendMsg(e) {
  if (e) e.preventDefault()
  const content = inputText.value.trim()
  if (!content) return

  const receiverId = parseInt(route.params.userId)
  chatStore.send(receiverId, null, content)
  inputText.value = ''
  messages.value = chatStore.messages
  nextTick(() => scrollToBottom())
}

function scrollToBottom() {
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-window {
  max-width: 700px;
  margin: 0 auto;
}

.chat-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  overflow: hidden;
}

.chat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.back-btn {
  background: none;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
}
.back-btn:hover { color: var(--green-500); }

.partner-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  min-height: 300px;
}

.msg-item {
  margin-bottom: 16px;
  display: flex;
}

.msg-me { justify-content: flex-end; }
.msg-other { justify-content: flex-start; }

.msg-bubble {
  max-width: 70%;
  padding: 10px 16px;
  border-radius: 14px;
}

.msg-me .msg-bubble {
  background: var(--green-500);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-other .msg-bubble {
  background: #f0f0f0;
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
}

.msg-content {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.msg-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.7;
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 14px 20px;
  border-top: 1px solid var(--border-lighter);
  align-items: flex-end;
}

.msg-input {
  flex: 1;
  padding: 10px 14px;
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  outline: none;
  resize: none;
  font-family: inherit;
  transition: border-color var(--transition-fast);
}
.msg-input:focus { border-color: var(--green-500); }

.send-btn {
  padding: 10px 24px;
  white-space: nowrap;
  flex-shrink: 0;
}
</style>
