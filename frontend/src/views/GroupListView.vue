<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="d-flex align-items-center">
        <router-link to="/home" class="btn btn-outline-secondary me-3">&larr; 홈</router-link>
        <h3 class="mb-0">소분 그룹</h3>
      </div>
      <router-link to="/groups/new" class="btn btn-primary">+ 그룹 생성</router-link>
    </div>

    <!-- 탭 (전체 / 내 그룹) -->
    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <button
          class="nav-link"
          :class="{ active: currentTab === 'all' }"
          @click="currentTab = 'all'"
        >
          전체 그룹
        </button>
      </li>
      <li class="nav-item">
        <button
          class="nav-link"
          :class="{ active: currentTab === 'my' }"
          @click="currentTab = 'my'"
        >
          내 그룹
        </button>
      </li>
    </ul>

    <!-- 로딩 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩중...</span>
      </div>
    </div>

    <!-- 그룹 목록 -->
    <div v-else>
      <!-- 전체 그룹 -->
      <div v-if="currentTab === 'all'">
        <div v-if="groups.length === 0" class="alert alert-info">
          아직 생성된 그룹이 없습니다.
        </div>
        <div v-else class="row g-3">
          <div v-for="group in groups" :key="group.id" class="col-12 col-md-6 col-lg-4">
            <div class="card h-100 shadow-sm group-card" @click="goToDetail(group.id)">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <h5 class="card-title mb-0">{{ group.title }}</h5>
                  <span class="badge" :class="getStatusBadgeClass(group.status)">
                    {{ getStatusText(group.status) }}
                  </span>
                </div>
                <p class="card-text text-muted small mb-2">
                  <i class="bi bi-geo-alt"></i> {{ group.pickupLocation || '미정' }}
                </p>
                <div class="d-flex justify-content-between align-items-center">
                  <span class="text-primary fw-bold">
                    {{ formatPrice(group.totalPrice) }}원
                  </span>
                  <span class="badge bg-secondary">
                    {{ group.currentParticipants }}/{{ group.maxParticipants }}명
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 페이지네이션 -->
        <nav v-if="totalPages > 1" class="mt-4">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <button class="page-link" @click="changePage(currentPage - 1)">&laquo;</button>
            </li>
            <li
              v-for="page in totalPages"
              :key="page"
              class="page-item"
              :class="{ active: currentPage === page - 1 }"
            >
              <button class="page-link" @click="changePage(page - 1)">{{ page }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
              <button class="page-link" @click="changePage(currentPage + 1)">&raquo;</button>
            </li>
          </ul>
        </nav>
      </div>

      <!-- 내 그룹 -->
      <div v-else>
        <div v-if="myGroups.length === 0" class="alert alert-info">
          참여 중인 그룹이 없습니다.
        </div>
        <div v-else class="list-group">
          <div
            v-for="group in myGroups"
            :key="group.id"
            class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
            @click="goToDetail(group.id)"
            style="cursor: pointer;"
          >
            <div>
              <h6 class="mb-1">{{ group.title }}</h6>
              <small class="text-muted">
                <span class="badge" :class="getStatusBadgeClass(group.status)">
                  {{ getStatusText(group.status) }}
                </span>
                <span class="ms-2">{{ group.pickupLocation || '위치 미정' }}</span>
              </small>
            </div>
            <div class="text-end">
              <span class="badge bg-primary">{{ group.currentParticipants }}/{{ group.maxParticipants }}</span>
              <router-link
                :to="`/chat/${group.id}`"
                class="btn btn-sm btn-outline-secondary ms-2"
                @click.stop
              >
                채팅
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getGroups, getMyGroups } from '../api/group'

export default {
  name: 'GroupListView',

  data() {
    return {
      currentTab: 'all',
      loading: false,
      // 전체 그룹
      groups: [],
      currentPage: 0,
      totalPages: 0,
      // 내 그룹
      myGroups: []
    }
  },

  watch: {
    currentTab(newTab) {
      if (newTab === 'all') {
        this.loadAllGroups()
      } else {
        this.loadMyGroups()
      }
    }
  },

  async mounted() {
    await this.loadAllGroups()
    await this.loadMyGroups()
  },

  methods: {
    async loadAllGroups() {
      this.loading = true
      try {
        const response = await getGroups(this.currentPage, 9)
        const data = response.data
        this.groups = data.content || []
        this.totalPages = data.totalPages || 0
      } catch (error) {
        console.error('그룹 목록 로드 실패:', error)
      } finally {
        this.loading = false
      }
    },

    async loadMyGroups() {
      this.loading = true
      try {
        const response = await getMyGroups()
        this.myGroups = response.data|| []
      } catch (error) {
        console.error('내 그룹 로드 실패:', error)
      } finally {
        this.loading = false
      }
    },

    changePage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page
        this.loadAllGroups()
      }
    },

    goToDetail(groupId) {
      this.$router.push(`/groups/${groupId}`)
    },

    getStatusText(status) {
      const map = {
        RECRUITING: '모집중',
        FULL: '모집완료',
        CANCELLED: '취소됨'
      }
      return map[status] || status
    },

    getStatusBadgeClass(status) {
      const map = {
        RECRUITING: 'bg-success',
        FULL: 'bg-secondary',
        CANCELLED: 'bg-danger'
      }
      return map[status] || 'bg-secondary'
    },

    formatPrice(price) {
      return price?.toLocaleString() || '0'
    }
  }
}
</script>

<style scoped>
.group-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.group-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}
.nav-link {
  cursor: pointer;
}
</style>
