<!--
  파일: NavBar.vue
  설명: 공통 네비게이션 바 컴포넌트
        - 모든 페이지 상단에 표시 (로그인/회원가입 페이지 제외)
        - NearSplit 로고 클릭 → /home으로 이동
        - 카테고리 메뉴: 공동구매(/groups), 상품(/products)
        - 알림 아이콘 + 읽지 않은 알림 개수 배지
        - 알림 드롭다운 (최근 알림 목록)
        - 프로필 드롭다운 (프로필, 그룹 목록, 로그아웃)
-->
<template>
  <nav class="navbar navbar-light bg-white border-bottom sticky-top">
    <div class="container">
      <!-- 왼쪽: 홈 로고/링크 + 카테고리 메뉴 -->
      <div class="d-flex align-items-center">
        <!-- NearSplit 로고: 클릭 시 홈으로 이동 -->
        <router-link to="/home" class="navbar-brand d-flex align-items-center me-4">
          <span class="fw-bold text-primary">NearSplit</span>
        </router-link>

        <!-- 카테고리 메뉴: 공동구매 / 상품 -->
        <ul class="nav nav-pills">
          <li class="nav-item">
            <router-link
              to="/groups"
              class="nav-link"
              :class="{ 'active': isGroupsActive }"
            >
              공동구매
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/products"
              class="nav-link"
              :class="{ 'active': isProductsActive }"
            >
              상품
            </router-link>
          </li>
        </ul>
      </div>

      <!-- 오른쪽: 알림 + 프로필 -->
      <div class="d-flex align-items-center">

        <!-- ==================== 알림 드롭다운 ==================== -->
        <div class="dropdown me-3">
          <!--
            알림 벨 아이콘 버튼
            position-relative: 배지를 아이콘 위에 위치시키기 위함
          -->
          <button
            class="btn btn-outline-secondary position-relative"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <!-- 벨 아이콘 (Bootstrap Icons) -->
            <i class="bi bi-bell"></i>
            <!--
              읽지 않은 알림 배지
              unreadCount > 0 일 때만 표시
              position-absolute: 아이콘 우상단에 위치
            -->
            <span
              v-if="unreadCount > 0"
              class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
            >
              <!-- 99개 초과 시 99+로 표시 -->
              {{ unreadCount > 99 ? '99+' : unreadCount }}
              <span class="visually-hidden">읽지 않은 알림</span>
            </span>
          </button>

          <!-- 알림 드롭다운 메뉴 -->
          <ul class="dropdown-menu dropdown-menu-end notification-dropdown">
            <!-- 드롭다운 헤더 -->
            <li class="dropdown-header d-flex justify-content-between align-items-center">
              <span class="fw-bold">알림</span>
              <!-- 읽지 않은 알림이 있을 때만 '모두 읽음' 버튼 표시 -->
              <button
                v-if="unreadCount > 0"
                class="btn btn-link btn-sm p-0"
                @click.stop="markAllAsReadLocal"
              >
                모두 읽음
              </button>
            </li>
            <li><hr class="dropdown-divider"></li>

            <!-- 알림 목록 (최대 5개만 표시) -->
            <li v-if="notifications.length === 0" class="dropdown-item text-muted text-center py-3">
              알림이 없습니다
            </li>

            <!--
              알림 아이템 반복 렌더링
              unread 클래스: 읽지 않은 알림에 연한 파란 배경 적용
              주의: 백엔드에서 boolean isRead → Jackson이 "read"로 직렬화
                    (Java boolean의 is 접두사 제거 규칙)
            -->
            <li
              v-for="notification in recentNotifications"
              :key="notification.id"
              class="notification-item"
              :class="{ 'unread': !notification.read }"
              @click="handleNotificationClick(notification)"
            >
              <div class="dropdown-item d-flex align-items-start">
                <!-- 알림 아이콘 (타입별로 다름) -->
                <span class="notification-icon me-2">
                  {{ getNotificationIcon(notification.type) }}
                </span>
                <div class="flex-grow-1">
                  <!-- 알림 제목 -->
                  <div class="notification-title">{{ notification.title }}</div>
                  <!-- 알림 내용 (2줄 말줄임) -->
                  <small class="text-muted notification-message">
                    {{ notification.message }}
                  </small>
                  <!-- 시간 표시 -->
                  <small class="text-muted d-block mt-1">
                    {{ formatTimeAgo(notification.createdAt) }}
                  </small>
                </div>
                <!--
                  읽지 않음 표시 (파란 점)
                  notification.read: 백엔드 boolean isRead가 "read"로 직렬화됨
                -->
                <span v-if="!notification.read" class="unread-dot"></span>
              </div>
            </li>

            <!-- 더보기 링크 (알림이 5개 초과일 때) -->
            <li v-if="notifications.length > 5">
              <hr class="dropdown-divider">
              <router-link to="/notifications" class="dropdown-item text-center text-primary">
                모든 알림 보기
              </router-link>
            </li>
          </ul>
        </div>

        <!-- ==================== 프로필 드롭다운 ==================== -->
        <div class="dropdown">
          <button
            class="btn btn-outline-secondary dropdown-toggle d-flex align-items-center"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <!-- 프로필 아이콘 (닉네임 첫 글자) -->
            <span
              class="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center me-2"
              style="width: 28px; height: 28px; font-size: 0.8rem;"
            >
              {{ userInitial }}
            </span>
            {{ userNickname }}
          </button>
          <!-- 드롭다운 메뉴 -->
          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <router-link to="/profile" class="dropdown-item">
                <i class="bi bi-person me-2"></i>프로필
              </router-link>
            </li>
            <li>
              <router-link to="/groups" class="dropdown-item">
                <i class="bi bi-collection me-2"></i>그룹 목록
              </router-link>
            </li>
            <li><hr class="dropdown-divider"></li>
            <li>
              <button class="dropdown-item text-danger" @click="handleLogout">
                <i class="bi bi-box-arrow-right me-2"></i>로그아웃
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
// API 함수 import
import { logout } from '../api/auth'
import { markAllAsRead } from '../api/notification'

export default {
  name: 'NavBar',

  // ==================== Props ====================
  // 부모 컴포넌트(App.vue)에서 전달받는 데이터
  props: {
    // 현재 로그인한 사용자 정보
    user: {
      type: Object,
      default: null
    },
    // 읽지 않은 알림 개수 (App.vue에서 관리)
    unreadCount: {
      type: Number,
      default: 0
    },
    // 알림 목록 (App.vue에서 관리)
    notifications: {
      type: Array,
      default: () => []
    }
  },

  // ==================== Emits ====================
  // 부모 컴포넌트로 전달하는 이벤트
  // mark-notification-read: 단일 알림 읽음 처리
  // mark-all-read: 모든 알림 읽음 처리 (unreadCount를 0으로 만들기 위함)
  emits: ['logout', 'mark-notification-read', 'mark-all-read'],

  // ==================== Computed ====================
  computed: {
    // 사용자 닉네임 (없으면 '사용자')
    userNickname() {
      return this.user?.nickname || '사용자'
    },
    // 프로필 아이콘에 표시할 첫 글자
    userInitial() {
      return this.userNickname.charAt(0).toUpperCase()
    },
    // 드롭다운에 표시할 최근 알림 (최대 5개)
    recentNotifications() {
      return this.notifications.slice(0, 5)
    },
    // 현재 경로가 공동구매(그룹) 관련 페이지인지 확인
    // /groups 또는 /groups/로 시작하는 경로 + /chat 경로 포함
    isGroupsActive() {
      const path = this.$route.path
      return path.startsWith('/groups') || path.startsWith('/chat')
    },
    // 현재 경로가 상품 관련 페이지인지 확인
    // /products 또는 /products/로 시작하는 경로
    isProductsActive() {
      return this.$route.path.startsWith('/products')
    }
  },

  // ==================== Methods ====================
  methods: {
    /**
     * 로그아웃 처리
     * 1. 백엔드 로그아웃 API 호출
     * 2. 부모 컴포넌트에 logout 이벤트 emit (WebSocket 해제용)
     * 3. localStorage 정리 후 로그인 페이지로 이동
     */
    async handleLogout() {
      try {
        await logout()
      } catch (error) {
        console.error('로그아웃 API 실패:', error)
      } finally {
        // 부모 컴포넌트(App.vue)에 로그아웃 이벤트 전달
        // App.vue에서 WebSocket 연결 해제 처리
        this.$emit('logout')

        // 클라이언트 측 로그아웃 처리
        localStorage.removeItem('isLoggedIn')
        this.$router.push('/login')
      }
    },

    /**
     * 알림 클릭 처리
     * 1. 읽지 않은 알림이면 읽음 처리 (부모에게 emit)
     * 2. referenceId가 있으면 해당 페이지로 이동
     * @param {Object} notification - 알림 객체
     */
    handleNotificationClick(notification) {
      // 읽지 않은 알림이면 읽음 처리
      // 주의: 백엔드 boolean isRead → Jackson이 "read"로 직렬화
      if (!notification.read) {
        this.$emit('mark-notification-read', notification.id)
      }

      // referenceId가 있으면 해당 페이지로 이동
      // TODO: referenceType에 따라 다른 페이지로 분기 (현재는 모두 그룹 상세)
      if (notification.referenceId) {
        const targetPath = `/groups/${notification.referenceId}`

        // 같은 페이지에서 알림 클릭 시에도 데이터 새로고침되도록
        // query 파라미터에 timestamp 추가하여 라우트 변경 감지
        // 예: /groups/5 → /groups/5?t=1706512345678
        this.$router.push({
          path: targetPath,
          query: { t: Date.now() }
        })
      }
    },

    /**
     * 모든 알림 읽음 처리 (로컬 + 백엔드)
     * 1. 백엔드 API 호출 (PATCH /api/notifications/read-all)
     * 2. 부모 컴포넌트(App.vue)에 mark-all-read 이벤트 emit
     *    - 부모에서 unreadNotificationCount = 0 처리
     *    - 부모에서 모든 notifications의 isRead = true 처리
     * 주의: props를 직접 수정하면 안 됨! 부모에게 위임해야 함
     */
    async markAllAsReadLocal() {
      try {
        // 백엔드 API 호출
        await markAllAsRead()

        // 부모 컴포넌트에 이벤트 emit
        // App.vue에서 unreadNotificationCount = 0 및 모든 알림 isRead = true 처리
        this.$emit('mark-all-read')

        console.log('모든 알림 읽음 처리 완료')
      } catch (error) {
        console.error('모든 알림 읽음 처리 실패:', error)
      }
    },

    /**
     * 알림 타입별 아이콘 반환
     * @param {string} type - 알림 타입 (NotificationType enum)
     * @returns {string} 이모지 아이콘
     */
    getNotificationIcon(type) {
      // 백엔드 NotificationType enum에 맞춰서 매핑
      const iconMap = {
        'JOIN_REQUEST': '📥',      // 참여 신청
        'JOIN_APPROVED': '✅',     // 참여 승인
        'JOIN_REJECTED': '❌',     // 참여 거절
        'GROUP_FULL': '🎉',        // 모집 완료
        'GROUP_CANCELLED': '🚫',   // 그룹 취소
        'CHAT_MESSAGE': '💬',      // 새 채팅 메시지
        'SYSTEM': '📢'             // 시스템 알림
      }
      return iconMap[type] || '🔔'
    },

    /**
     * 시간을 "~전" 형식으로 변환
     * @param {string} dateString - ISO 형식 날짜 문자열
     * @returns {string} "방금 전", "5분 전", "1시간 전" 등
     */
    formatTimeAgo(dateString) {
      if (!dateString) return ''

      const now = new Date()
      const date = new Date(dateString)
      const diffMs = now - date
      const diffSec = Math.floor(diffMs / 1000)
      const diffMin = Math.floor(diffSec / 60)
      const diffHour = Math.floor(diffMin / 60)
      const diffDay = Math.floor(diffHour / 24)

      if (diffSec < 60) return '방금 전'
      if (diffMin < 60) return `${diffMin}분 전`
      if (diffHour < 24) return `${diffHour}시간 전`
      if (diffDay < 7) return `${diffDay}일 전`

      // 7일 이상이면 날짜 표시
      return date.toLocaleDateString('ko-KR')
    }
  }
}
</script>

<style scoped>
/* 네비게이션 바 기본 스타일 */
.navbar {
  padding: 0.75rem 0;
}
.navbar-brand {
  text-decoration: none;
}

/* ==================== 알림 드롭다운 스타일 ==================== */

/* 알림 드롭다운 메뉴: 최대 너비 및 최대 높이 설정 */
.notification-dropdown {
  width: 320px;
  max-height: 400px;
  overflow-y: auto;  /* 스크롤 가능 */
}

/* 알림 아이템 기본 스타일 */
.notification-item {
  cursor: pointer;
  transition: background-color 0.15s ease;
}

/* 알림 아이템 호버 시 배경색 */
.notification-item:hover {
  background-color: #f8f9fa;
}

/* 읽지 않은 알림: 연한 파란 배경 */
.notification-item.unread {
  background-color: #e7f1ff;
}

/* 알림 아이콘 (이모지) */
.notification-icon {
  font-size: 1.2rem;
  min-width: 24px;
}

/* 알림 제목: 1줄 말줄임 */
.notification-title {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 알림 메시지: 2줄 말줄임 */
.notification-message {
  display: -webkit-box;
  -webkit-line-clamp: 2;          /* 최대 2줄 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 읽지 않음 표시 (파란 점) */
.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #0d6efd;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}

/* 알림 배지 (읽지 않은 개수) */
.badge.rounded-pill {
  font-size: 0.65rem;
  min-width: 18px;
  padding: 0.25rem 0.4rem;
}

/* ==================== 카테고리 메뉴 스타일 ==================== */

/* 카테고리 nav 링크 기본 스타일 */
.nav-pills .nav-link {
  color: #6c757d;  /* 회색 텍스트 */
  font-weight: 500;
  padding: 0.4rem 1rem;
  border-radius: 0.375rem;
  transition: all 0.15s ease;
}

/* 호버 시 배경색 */
.nav-pills .nav-link:hover {
  color: #0d6efd;
  background-color: #e7f1ff;
}

/* 활성화된 카테고리: 파란 배경 + 흰 글씨 */
.nav-pills .nav-link.active {
  color: #fff;
  background-color: #0d6efd;
}
</style>
