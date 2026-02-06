<!--
  파일: App.vue
  설명: Vue 애플리케이션의 루트 컴포넌트
        - 모든 페이지의 최상위 부모 컴포넌트
        - NavBar 표시 여부 결정:
          1. 로그인 필요 페이지에서만 (requiresAuth: true)
          2. 팝업 창이 아닐 때만 (채팅 팝업에서는 NavBar 숨김)
        - 로그인 시 WebSocket 연결 관리 (알림 수신용)
        - 사용자 프로필 정보 관리
-->
<template>
  <div id="app">
    <!--
      NavBar 컴포넌트
      로그인이 필요한 페이지에서만 표시 (requiresAuth: true)
      로그인/회원가입 페이지에서는 숨김
      @logout: 로그아웃 시 WebSocket 연결 해제
    -->
    <!--
      NavBar 컴포넌트에 전달하는 이벤트 핸들러:
      @logout: 로그아웃 시 WebSocket 연결 해제
      @mark-notification-read: 단일 알림 읽음 처리
      @mark-all-read: 전체 알림 읽음 처리 (배지 숫자 + 파란점 모두 업데이트)
    -->
    <NavBar
      v-if="showNavBar"
      :user="currentUser"
      :unread-count="unreadNotificationCount"
      :notifications="notifications"
      @logout="handleLogout"
      @mark-notification-read="markNotificationAsRead"
      @mark-all-read="markAllNotificationsAsRead"
    />

    <!-- 페이지 컨텐츠 영역 -->
    <router-view />
  </div>
</template>

<script>
// ==================== 라이브러리 import ====================
// SockJS: WebSocket의 폴백(fallback) 라이브러리
import SockJS from 'sockjs-client'

// STOMP: WebSocket 위에서 동작하는 메시징 프로토콜
import { Client } from '@stomp/stompjs'

// 컴포넌트 및 API import
import NavBar from './components/NavBar.vue'
import { getMyProfile } from './api/user'
import { getNotifications, getUnreadCount, markAsRead } from './api/notification'

export default {
  name: 'App',

  components: {
    NavBar
  },

  // ==================== 컴포넌트 상태 (data) ====================
  data() {
    return {
      currentUser: null,              // 현재 로그인한 사용자 정보
      stompClient: null,              // STOMP 클라이언트 인스턴스
      wsConnected: false,             // WebSocket 연결 상태
      notifications: [],              // 알림 목록
      unreadNotificationCount: 0      // 읽지 않은 알림 개수
    }
  },

  // ==================== 계산된 속성 (computed) ====================
  computed: {
    // 현재 라우트에서 NavBar를 표시할지 결정
    // 조건 1: requiresAuth: true인 페이지에서만 NavBar 표시
    // 조건 2: 팝업 창이 아닐 때만 표시 (window.opener가 없을 때)
    //         채팅 팝업처럼 별도 창으로 열린 경우 NavBar 숨김
    showNavBar() {
      const requiresAuth = this.$route.meta.requiresAuth === true
      const isPopup = !!window.opener  // 팝업 창 여부 확인
      return requiresAuth && !isPopup
    }
  },

  // ==================== 감시자 (watch) ====================
  watch: {
    // 라우트가 변경될 때마다 인증 상태 확인 및 WebSocket 관리
    '$route.meta.requiresAuth': {
      immediate: true,  // 컴포넌트 생성 시 즉시 실행
      async handler(requiresAuth) {
        if (requiresAuth && !this.currentUser) {
          // 로그인 필요 페이지 진입 + 사용자 정보 없음 → 프로필 로드
          await this.loadUserProfile()
          // 사용자 정보 로드 후 WebSocket 연결
          if (this.currentUser && !this.wsConnected) {
            this.connectNotificationWebSocket()
            await this.loadNotifications()
          }
        } else if (!requiresAuth && this.wsConnected) {
          // 로그인 불필요 페이지 진입 (로그아웃) → WebSocket 해제
          this.disconnectNotificationWebSocket()
        }
      }
    }
  },

  // ==================== 라이프사이클 훅 ====================
  // 컴포넌트가 DOM에서 제거되기 전 WebSocket 정리
  beforeUnmount() {
    this.disconnectNotificationWebSocket()
  },

  // ==================== 메서드 ====================
  methods: {
    /**
     * 현재 로그인한 사용자 프로필 로드
     * GET /api/users/me 호출
     */
    async loadUserProfile() {
      try {
        const response = await getMyProfile()
        // UserController는 ApiResponse로 감싸지 않고 직접 반환
        this.currentUser = response.data
        console.log('사용자 프로필 로드 완료:', this.currentUser)
      } catch (error) {
        console.error('사용자 프로필 로드 실패:', error)
        // 인증 실패 시 로그인 페이지로 이동
        if (error.response?.status === 401) {
          localStorage.removeItem('isLoggedIn')
          this.$router.push('/login')
        }
      }
    },

    /**
     * 알림 목록 및 읽지 않은 개수 로드
     * GET /api/notifications, GET /api/notifications/unread-count 호출
     */
    async loadNotifications() {
      try {
        // 병렬로 알림 목록과 읽지 않은 개수 조회
        const [notificationsRes, countRes] = await Promise.all([
          getNotifications(),
          getUnreadCount()
        ])

        // 응답 데이터 저장
        // 백엔드 ApiResponse 구조: { success: true, data: 실제데이터 }
        // axios 응답: { data: { success, data } }
        // 따라서 실제 데이터는 response.data.data 에 있음
        this.notifications = notificationsRes.data?.data || []
        // unreadCount는 int 직접 반환이면 .data, ApiResponse면 .data.data
        // 백엔드 확인 필요 - 일단 둘 다 처리
        this.unreadNotificationCount = typeof countRes.data === 'number'
          ? countRes.data
          : (countRes.data?.data || 0)

        console.log('알림 로드 완료:', this.notifications.length, '개')
        console.log('읽지 않은 알림:', this.unreadNotificationCount, '개')
      } catch (error) {
        console.error('알림 로드 실패:', error)
      }
    },

    /**
     * 알림용 WebSocket 연결
     * /topic/notification/{userId} 구독
     *
     * 주의: 팝업 창(결제, 채팅 등)에서는 연결하지 않음
     *       - 불필요한 서버 리소스 낭비 방지
     *       - 메인 창에서만 알림 수신하면 충분
     */
    connectNotificationWebSocket() {
      // 팝업 창에서는 WebSocket 연결 생략
      // window.opener가 존재하면 이 창은 다른 창에서 열린 팝업
      if (window.opener) {
        console.log('팝업 창에서는 알림 WebSocket 연결 생략')
        return
      }

      // 사용자 정보 없으면 연결 불가
      if (!this.currentUser?.id) {
        console.warn('WebSocket 연결 실패: 사용자 정보 없음')
        return
      }

      // 1. SockJS 소켓 생성 (백엔드 WebSocket 엔드포인트)
      const socket = new SockJS('http://localhost:8080/ws')

      // 2. STOMP 클라이언트 생성 및 설정
      this.stompClient = new Client({
        // WebSocket 팩토리: SockJS 소켓 반환
        webSocketFactory: () => socket,

        // 디버그 로그 (개발 중에만 사용, 배포 시 제거 권장)
        debug: (str) => console.log('STOMP [Notification]:', str),

        // 연결 끊기면 5초 후 자동 재연결 시도
        reconnectDelay: 5000,

        // 하트비트: 연결 상태 확인용 (4초 간격)
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      })

      // 3. 연결 성공 시 콜백
      this.stompClient.onConnect = () => {
        console.log('알림 WebSocket 연결됨')
        this.wsConnected = true

        // 알림 토픽 구독: /topic/notification/{userId}
        // 백엔드 NotificationService에서 이 경로로 메시지 발행
        this.stompClient.subscribe(
          `/topic/notification/${this.currentUser.id}`,
          (message) => {
            // 수신된 응답을 JSON 파싱
            const response = JSON.parse(message.body)
            console.log('알림 WebSocket 수신:', response)

            // DELETE 액션인 경우: 알림 목록에서 제거
            if (response.action === 'DELETE') {
              // 삭제할 알림 찾기
              const targetIndex = this.notifications.findIndex(
                n => n.id === response.notificationId
              )
              if (targetIndex !== -1) {
                // 읽지 않은 알림이었으면 카운트 감소
                // 주의: 백엔드 boolean isRead → Jackson이 "read"로 직렬화
                if (!this.notifications[targetIndex].read) {
                  this.unreadNotificationCount--
                }
                // 목록에서 제거
                this.notifications.splice(targetIndex, 1)
                console.log('알림 삭제됨:', response.notificationId)
              }
            } else {
              // 새 알림: 목록 맨 앞에 추가 (최신순)
              this.notifications.unshift(response)
              // 읽지 않은 개수 증가
              this.unreadNotificationCount++
              console.log('새 알림 추가됨:', response)
            }
          }
        )
      }

      // 4. 연결 끊김 시 콜백
      this.stompClient.onDisconnect = () => {
        console.log('알림 WebSocket 연결 끊김')
        this.wsConnected = false
      }

      // 5. STOMP 에러 발생 시 콜백
      this.stompClient.onStompError = (frame) => {
        console.error('알림 WebSocket STOMP 에러:', frame)
        this.wsConnected = false
      }

      // 6. 연결 시작
      this.stompClient.activate()
    },

    /**
     * 알림용 WebSocket 연결 해제
     * 로그아웃 또는 페이지 이탈 시 호출
     */
    disconnectNotificationWebSocket() {
      if (this.stompClient && this.wsConnected) {
        this.stompClient.deactivate()
        this.wsConnected = false
        console.log('알림 WebSocket 연결 해제됨')
      }
    },

    /**
     * 로그아웃 처리 (NavBar에서 emit)
     * WebSocket 연결 해제 및 상태 초기화
     */
    handleLogout() {
      // WebSocket 연결 해제
      this.disconnectNotificationWebSocket()
      // 상태 초기화
      this.currentUser = null
      this.notifications = []
      this.unreadNotificationCount = 0
    },

    /**
     * 알림 읽음 처리 (NavBar에서 emit)
     * @param {number} notificationId - 알림 ID
     */
    async markNotificationAsRead(notificationId) {
      try {
        // 백엔드 API 호출: PATCH /api/notifications/{id}/read
        await markAsRead(notificationId)

        // 로컬 상태 업데이트: 해당 알림의 read를 true로 변경
        // 주의: 백엔드 boolean isRead → Jackson이 "read"로 직렬화
        const notification = this.notifications.find(n => n.id === notificationId)
        if (notification && !notification.read) {
          notification.read = true
          this.unreadNotificationCount--
        }
      } catch (error) {
        console.error('알림 읽음 처리 실패:', error)
      }
    },

    /**
     * 전체 알림 읽음 처리 (NavBar에서 emit)
     * NavBar에서 백엔드 API 호출 완료 후 호출됨
     * 로컬 상태만 업데이트: 배지 숫자 0, 모든 알림 read = true
     */
    markAllNotificationsAsRead() {
      // 모든 알림의 read를 true로 변경
      // NavBar에서 이미 백엔드 API 호출 완료했으므로 로컬만 업데이트
      // 주의: 백엔드 boolean isRead → Jackson이 "read"로 직렬화
      this.notifications.forEach(notification => {
        notification.read = true
      })
      // 읽지 않은 알림 개수를 0으로 설정
      this.unreadNotificationCount = 0
      console.log('전체 알림 읽음 처리 (로컬 상태 업데이트)')
    }
  }
}
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

body {
  margin: 0;
  padding: 0;
}
</style>
