<template>
  <!-- 임시 채팅 페이지 -->
  <div class="container mt-4">
    <h2>채팅 페이지 (미구현)</h2>
    <p>그룹 ID: {{ groupId }}</p>
    <router-link to="/groups" class="btn btn-secondary">목록으로</router-link>
  </div>
</template>

<script>
export default {
  name: 'ChatView',
  computed: {
    // URL의 :groupId 파라미터 가져오기
    groupId() {
      return this.$route.params.groupId
    }
  }
}
</script>
