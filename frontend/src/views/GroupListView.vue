<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>소분 그룹 목록</h2>
      <button class="btn btn-danger" @click="handleLogout">로그아웃</button>
    </div>

    <div class="alert alert-success" role="alert">
      <h4 class="alert-heading">로그인 성공!</h4>
      <p>그룹 목록 페이지입니다. (아직 미구현)</p>
      <hr>
      <p class="mb-0">다음 단계에서 실제 그룹 목록을 불러오는 기능을 구현할 예정입니다.</p>
    </div>
  </div>
</template>

<script>
import { logout } from '../api/auth'

export default {
  name: 'GroupListView',
  methods: {
    async handleLogout() {
      try {
        await logout()
        this.$router.push('/login')
      } catch (error) {
        console.error('로그아웃 실패:', error)
        // 실패해도 로그인 페이지로 이동
        this.$router.push('/login')
      }
    }
  }
}
</script>
