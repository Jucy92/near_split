<template>
  <div class="container-fluid py-4" style="height: 100vh;">
    <div class="row h-100">
      <div class="col-12 col-lg-8 offset-lg-2">
        <div class="card shadow h-100 d-flex flex-column">
          <!-- 헤더 -->
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center">
              <router-link :to="`/groups/${groupId}`" class="btn btn-outline-secondary btn-sm me-3">
                &larr; 그룹
              </router-link>
              <h5 class="mb-0">채팅방</h5>
              <span
                class="badge ms-2"
                :class="connected ? 'bg-success' : 'bg-danger'"
              >
                {{ connected ? '연결됨' : '연결 끊김' }}
              </span>
            </div>
            <small class="text-muted">그룹 #{{ groupId }}</small>
          </div>

          <!-- 메시지 영역 -->
          <div
            ref="messageContainer"
            class="card-body flex-grow-1 overflow-auto"
            style="background: #f8f9fa;"
          >
            <!-- 로딩 -->
            <div v-if="loading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">로딩중...</span>
              </div>
            </div>

            <!-- 메시지 목록 -->
            <div v-else>
              <div v-if="messages.length === 0" class="text-center text-muted py-5">
                아직 메시지가 없습니다. 첫 메시지를 보내보세요!
              </div>

              <div
                v-for="(message, index) in messages"
                :key="index"
                class="mb-3"
                :class="getMessageAlignment(message)"
              >
                <!-- 시스템 메시지 (JOIN, LEAVE, NOTICE) -->
                <div v-if="message.type !== 'CHAT'" class="text-center">
                  <small class="text-muted bg-white px-3 py-1 rounded">
                    {{ getSystemMessage(message) }}
                  </small>
                </div>

                <!-- 일반 채팅 메시지 -->
                <div v-else :class="isMyMessage(message) ? 'text-end' : ''">
                  <small v-if="!isMyMessage(message)" class="text-muted d-block mb-1">
                    {{ message.senderName }}
                  </small>
                  <div
                    class="d-inline-block p-2 px-3 rounded-3"
                    :class="isMyMessage(message) ? 'bg-primary text-white' : 'bg-white'"
                    style="max-width: 70%;"
                  >
                    {{ message.content }}
                  </div>
                  <small class="text-muted d-block mt-1">
                    {{ formatTime(message.createdAt) }}
                  </small>
                </div>
              </div>
            </div>
          </div>

          <!-- 입력 영역 -->
          <div class="card-footer bg-white">
            <form @submit.prevent="sendMessage" class="d-flex gap-2">
              <input
                type="text"
                class="form-control"
                v-model="newMessage"
                placeholder="메시지를 입력하세요..."
                :disabled="!connected"
              />
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="!connected || !newMessage.trim()"
              >
                전송
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { getRecentMessages } from '../api/chat'
import { getMyProfile } from '../api/user'

export default {
  name: 'ChatView',

  data() {
    return {
      messages: [],
      newMessage: '',
      loading: true,
      connected: false,
      stompClient: null,
      currentUser: null
    }
  },

  computed: {
    groupId() {
      return this.$route.params.groupId
    }
  },

  async mounted() {
    await this.loadInitialData()
    this.connectWebSocket()
  },

  beforeUnmount() {
    this.disconnectWebSocket()
  },

  methods: {
    async loadInitialData() {
      this.loading = true
      try {
        const [messagesRes, profileRes] = await Promise.all([
          getRecentMessages(this.groupId),
          getMyProfile()
        ])
        this.messages = messagesRes.data.data || []
        this.currentUser = profileRes.data.data
        this.$nextTick(() => this.scrollToBottom())
      } catch (error) {
        console.error('초기 데이터 로드 실패:', error)
      } finally {
        this.loading = false
      }
    },

    connectWebSocket() {
      const socket = new SockJS('http://localhost:8080/ws')

      this.stompClient = new Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log('STOMP:', str),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      })

      this.stompClient.onConnect = () => {
        console.log('WebSocket 연결됨')
        this.connected = true

        // 채팅방 구독
        this.stompClient.subscribe(`/topic/chat/${this.groupId}`, (message) => {
          const chatMessage = JSON.parse(message.body)
          this.messages.push(chatMessage)
          this.$nextTick(() => this.scrollToBottom())
        })

        // 입장 메시지 전송
        this.stompClient.publish({
          destination: `/app/chat/${this.groupId}/send`,
          body: JSON.stringify({
            groupId: this.groupId,
            content: `${this.currentUser?.name || '사용자'}님이 입장했습니다.`,
            type: 'JOIN'
          })
        })
      }

      this.stompClient.onDisconnect = () => {
        console.log('WebSocket 연결 끊김')
        this.connected = false
      }

      this.stompClient.onStompError = (frame) => {
        console.error('STOMP 에러:', frame)
        this.connected = false
      }

      this.stompClient.activate()
    },

    disconnectWebSocket() {
      if (this.stompClient && this.connected) {
        // 퇴장 메시지 전송
        this.stompClient.publish({
          destination: `/app/chat/${this.groupId}/send`,
          body: JSON.stringify({
            groupId: this.groupId,
            content: `${this.currentUser?.name || '사용자'}님이 퇴장했습니다.`,
            type: 'LEAVE'
          })
        })

        this.stompClient.deactivate()
      }
    },

    sendMessage() {
      if (!this.newMessage.trim() || !this.connected) return

      this.stompClient.publish({
        destination: `/app/chat/${this.groupId}/send`,
        body: JSON.stringify({
          groupId: this.groupId,
          content: this.newMessage.trim(),
          type: 'CHAT'
        })
      })

      this.newMessage = ''
    },

    isMyMessage(message) {
      return message.senderId === this.currentUser?.id
    },

    getMessageAlignment(message) {
      if (message.type !== 'CHAT') return ''
      return this.isMyMessage(message) ? '' : ''
    },

    getSystemMessage(message) {
      return message.content
    },

    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    },

    scrollToBottom() {
      const container = this.$refs.messageContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
.card {
  max-height: calc(100vh - 2rem);
}
</style>
