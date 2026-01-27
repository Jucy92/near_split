<template>
  <!--
    공통 네비게이션 바
    모든 페이지 상단에 표시 (로그인/회원가입 페이지 제외)
  -->
  <nav class="navbar navbar-light bg-white border-bottom sticky-top">
    <div class="container">
      <!-- 왼쪽: 홈 로고/링크 -->
      <router-link to="/groups" class="navbar-brand d-flex align-items-center">
        <span class="fw-bold text-primary">NearSplit</span>
      </router-link>

      <!-- 오른쪽: 프로필 드롭다운 -->
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
  </nav>
</template>

<script>
import { logout } from '../api/auth'

export default {
  name: 'NavBar',

  props: {
    // 부모 컴포넌트에서 사용자 정보를 전달받을 수 있음
    user: {
      type: Object,
      default: null
    }
  },

  computed: {
    // 사용자 닉네임 (없으면 '사용자')
    userNickname() {
      return this.user?.nickname || '사용자'
    },
    // 프로필 아이콘에 표시할 첫 글자
    userInitial() {
      return this.userNickname.charAt(0).toUpperCase()
    }
  },

  methods: {
    // 로그아웃 처리
    async handleLogout() {
      try {
        await logout()
      } catch (error) {
        console.error('로그아웃 API 실패:', error)
      } finally {
        // API 성공/실패 무관하게 클라이언트 측 로그아웃 처리
        localStorage.removeItem('isLoggedIn')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.navbar {
  padding: 0.75rem 0;
}
.navbar-brand {
  text-decoration: none;
}
</style>
