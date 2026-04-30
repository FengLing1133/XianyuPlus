<template>
  <div class="chat-window">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <el-button link @click="$router.push('/chat')">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <span class="partner-name">{{ partnerName }}</span>
        </div>
      </template>

      <!-- Messages area -->
      <div class="message-list" ref="msgListRef">
        <div v-for="msg in messages" :key="msg.id" :class="['msg-item', msg.senderId === currentUserId ? 'msg-me' : 'msg-other']">
          <div class="msg-bubble">
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-time">{{ msg.createdAt?.substring(0, 16) }}</div>
          </div>
        </div>
        <el-empty v-if="messages.length === 0" description="暂无消息，发送第一条吧" />
      </div>

      <!-- Input area -->
      <div class="input-area">
        <el-input v-model="inputText" type="textarea" :rows="3" placeholder="输入消息..." @keyup.enter.exact="sendMsg" />
        <el-button type="primary" @click="sendMsg" :disabled="!inputText.trim()">发送</el-button>
      </div>
    </el-card>
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

  // Load history
  const res = await request.get(`/message/${partnerId}`)
  messages.value = res.data

  // Get partner info
  try {
    const userRes = await request.get(`/user/${partnerId}`)
    partnerName.value = userRes.data.nickname
  } catch {}

  // Connect WebSocket
  chatStore.connect(currentUserId, userStore.token)
  chatStore.messages = messages.value

  scrollToBottom()
})

onUnmounted(() => {
  chatStore.disconnect()
})

function sendMsg() {
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
.chat-window { max-width: 700px; margin: 0 auto; }
.chat-card { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.chat-header { display: flex; align-items: center; gap: 10px; }
.partner-name { font-size: 16px; font-weight: 500; }
.message-list { flex: 1; overflow-y: auto; padding: 10px 0; min-height: 300px; }
.msg-item { margin-bottom: 15px; display: flex; }
.msg-me { justify-content: flex-end; }
.msg-other { justify-content: flex-start; }
.msg-bubble { max-width: 70%; padding: 10px 15px; border-radius: 8px; }
.msg-me .msg-bubble { background: #409eff; color: #fff; }
.msg-other .msg-bubble { background: #f0f0f0; color: #303133; }
.msg-content { font-size: 14px; line-height: 1.5; word-break: break-all; }
.msg-time { font-size: 11px; margin-top: 4px; opacity: 0.7; }
.input-area { display: flex; gap: 10px; align-items: flex-end; padding-top: 10px; border-top: 1px solid #ebeef5; }
.input-area .el-textarea { flex: 1; }
</style>
