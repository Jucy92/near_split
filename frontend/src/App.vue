<template>
  <div id="app">
    <!--
      NavBar 컴포넌트
      로그인이 필요한 페이지에서만 표시 (requiresAuth: true)
      로그인/회원가입 페이지에서는 숨김
    -->
    <NavBar v-if="showNavBar" :user="currentUser" />

    <!-- 페이지 컨텐츠 영역 -->
    <router-view />
  </div>
</template>

<script>
import NavBar from './components/NavBar.vue'
import { getMyProfile } from './api/user'

export default {
  name: 'App',

  components: {
    NavBar
  },

  data() {
    return {
      currentUser: null  // 현재 로그인한 사용자 정보
    }
  },

  computed: {
    // 현재 라우트에서 NavBar를 표시할지 결정
    // requiresAuth: true인 페이지에서만 NavBar 표시
    showNavBar() {
      return this.$route.meta.requiresAuth === true
    }
  },

  watch: {
    // 라우트가 변경될 때마다 NavBar 표시 여부에 따라 사용자 정보 로드
    '$route.meta.requiresAuth': {
      immediate: true,
      handler(requiresAuth) {
        if (requiresAuth && !this.currentUser) {
          this.loadUserProfile()
        }
      }
    }
  },

  methods: {
    // 현재 로그인한 사용자 프로필 로드
    async loadUserProfile() {
      try {
        const response = await getMyProfile()
        // UserController는 ApiResponse로 감싸지 않고 직접 반환
        this.currentUser = response.data
      } catch (error) {
        console.error('사용자 프로필 로드 실패:', error)
        // 인증 실패 시 로그인 페이지로 이동
        if (error.response?.status === 401) {
          localStorage.removeItem('isLoggedIn')
          this.$router.push('/login')
        }
      }
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
