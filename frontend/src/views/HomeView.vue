<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0">
        <span class="text-primary">NearSplit</span>
      </h2>
      <div class="d-flex gap-2">
        <router-link to="/profile" class="btn btn-outline-secondary btn-sm">
          {{ user?.nickname || '프로필' }}
        </router-link>
        <button class="btn btn-outline-danger btn-sm" @click="handleLogout">로그아웃</button>
      </div>
    </div>

    <!-- 환영 메시지 -->
    <div class="alert alert-primary mb-4" v-if="user">
      <h5 class="alert-heading mb-1">안녕하세요, {{ user.name }}님!</h5>
      <small class="text-muted">{{ user.email }}</small>
    </div>

    <!-- 메뉴 카드들 -->
    <div class="row g-4">
      <!-- 소분 그룹 -->
      <div class="col-12 col-md-6">
        <div class="card h-100 shadow-sm">
          <div class="card-body">
            <h5 class="card-title">
              <i class="bi bi-people-fill text-primary me-2"></i>
              소분 그룹
            </h5>
            <p class="card-text text-muted">공동구매 소분 그룹을 찾아보거나 직접 만들어보세요.</p>
            <div class="d-flex gap-2">
              <router-link to="/groups" class="btn btn-primary">그룹 목록</router-link>
              <router-link to="/groups/new" class="btn btn-outline-primary">그룹 생성</router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 상품 관리 -->
      <div class="col-12 col-md-6">
        <div class="card h-100 shadow-sm">
          <div class="card-body">
            <h5 class="card-title">
              <i class="bi bi-box-seam text-success me-2"></i>
              상품 관리
            </h5>
            <p class="card-text text-muted">소분할 상품을 등록하고 관리하세요.</p>
            <div class="d-flex gap-2">
              <router-link to="/products" class="btn btn-success">상품 목록</router-link>
              <router-link to="/products/new" class="btn btn-outline-success">상품 등록</router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 내 그룹 요약 -->
      <div class="col-12 col-md-6">
        <div class="card h-100 shadow-sm">
          <div class="card-body">
            <h5 class="card-title">
              <i class="bi bi-bookmark-star text-warning me-2"></i>
              내 그룹
            </h5>
            <p class="card-text text-muted">내가 참여 중인 그룹을 확인하세요.</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-warning text-dark fs-6">{{ myGroupCount }}개 참여 중</span>
              <router-link to="/groups" class="btn btn-outline-warning btn-sm">보러가기</router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 프로필 관리 -->
      <div class="col-12 col-md-6">
        <div class="card h-100 shadow-sm">
          <div class="card-body">
            <h5 class="card-title">
              <i class="bi bi-person-circle text-info me-2"></i>
              프로필
            </h5>
            <p class="card-text text-muted">프로필 정보를 확인하고 수정하세요.</p>
            <router-link to="/profile" class="btn btn-info text-white">프로필 보기</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 최근 그룹 목록 (간략) -->
    <div class="mt-5" v-if="recentGroups.length > 0">
      <h5 class="mb-3">최근 모집 중인 그룹</h5>
      <div class="list-group">
        <router-link
          v-for="group in recentGroups"
          :key="group.id"
          :to="`/groups/${group.id}`"
          class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
        >
          <div>
            <h6 class="mb-1">{{ group.title }}</h6>
            <small class="text-muted">{{ group.pickupLocation }}</small>
          </div>
          <span class="badge bg-primary rounded-pill">
            {{ group.currentParticipants }}/{{ group.maxParticipants }}
          </span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { logout } from '../api/auth'
import { getMyProfile } from '../api/user'
import { getGroups, getMyGroups } from '../api/group'

export default {
  name: 'HomeView',

  data() {
    return {
      user: null,
      myGroupCount: 0,
      recentGroups: [],
      loading: false
    }
  },

  // 컴포넌트가 마운트될 때 데이터 로드
  async mounted() {
    await this.loadData()
  },

  methods: {
    async loadData() {
      this.loading = true
      try {
        // 병렬로 데이터 로드
        const [profileRes, myGroupsRes, groupsRes] = await Promise.all([
          getMyProfile(),
          getMyGroups(),
          getGroups(0, 5)  // 최근 5개만
        ])

        this.user = profileRes.data.data
        this.myGroupCount = myGroupsRes.data.data?.length || 0
        this.recentGroups = groupsRes.data.data?.content || []
      } catch (error) {
        console.error('데이터 로드 실패:', error)
      } finally {
        this.loading = false
      }
    },

    async handleLogout() {
      try {
        await logout()
        localStorage.removeItem('isLoggedIn')
        this.$router.push('/login')
      } catch (error) {
        console.error('로그아웃 실패:', error)
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.card {
  transition: transform 0.2s, box-shadow 0.2s;
}
.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}
</style>
