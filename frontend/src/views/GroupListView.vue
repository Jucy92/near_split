<!--
  파일: GroupListView.vue
  설명: 소분 그룹 목록 페이지
        - 전체 그룹 목록 (페이징)
        - 내 그룹 목록 (참여 중인 그룹)
        - 탭으로 전환 가능

  ==================== 페이지 접근 흐름 ====================
  1. HomeView에서 "그룹 목록" 또는 "보러가기" 클릭
  2. router가 /groups로 이동
  3. GroupListView 렌더링
  4. mounted()에서 loadAllGroups() + loadMyGroups() 호출
  5. 기본 탭: "전체 그룹" (currentTab = 'all')
  6. 그룹 카드 클릭 → goToDetail(groupId) → /groups/{id}로 이동

  ==================== 데이터 흐름 ====================
  mounted() → 2개 API 순차 호출:
    - loadAllGroups() → GET /api/split?page=0&size=9 → this.groups
    - loadMyGroups()  → GET /api/split/my            → this.myGroups

  탭 전환 시 (watch: currentTab):
    - 'all'  → loadAllGroups()
    - 'my'   → loadMyGroups()

  ==================== API 목록 ====================
  | 기능             | 메서드 | 엔드포인트              | 호출 함수      |
  |------------------|--------|-------------------------|----------------|
  | 전체 그룹 목록   | GET    | /api/split?page=N&size=M| getGroups()    |
  | 내 그룹 목록     | GET    | /api/split/my           | getMyGroups()  |

  ==================== 백엔드 응답 구조 차이 ====================
  1. 전체 그룹 (GET /api/split) - SplitGroupResponse + Page
     {
       "content": [
         { "id": 1, "groupState": "RECRUITING", ... }
       ],
       "totalPages": 5,
       "totalElements": 45
     }
     - id 필드 사용
     - groupState 필드 사용

  2. 내 그룹 (GET /api/split/my) - SplitGroupSummaryResponse[]
     [
       { "groupId": 1, "status": "RECRUITING", "isHost": true, ... }
     ]
     - groupId 필드 사용 (id 아님!)
     - status 필드 사용 (groupState 아님!)
     - isHost: 방장 여부

  ==================== 컴포넌트 구조 ====================
  GroupListView
  ├── 상단 헤더 (홈 링크 + 그룹 생성 버튼)
  ├── 탭 (전체 그룹 / 내 그룹)
  ├── 전체 그룹 탭
  │   ├── 그룹 카드 그리드 (groups 배열 v-for)
  │   └── 페이지네이션
  └── 내 그룹 탭
      └── 리스트 그룹 (myGroups 배열 v-for)
-->
<template>
  <div class="container py-4">
    <!-- 상단 헤더: 홈 링크 + 페이지 제목 + 그룹 생성 버튼 -->
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
                  <!-- 백엔드 SplitGroupResponse는 groupState 필드 사용 -->
                  <span class="badge" :class="getStatusBadgeClass(group.groupState)">
                    {{ getStatusText(group.groupState) }}
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
          <!--
            내 그룹 목록: SplitGroupSummaryResponse 사용
            - groupId: 그룹 ID (id가 아님!)
            - status: 상태 (groupState가 아님!)
            - pickupLocation: 없음 (SummaryResponse에 없음)
          -->
          <div
            v-for="group in myGroups"
            :key="group.groupId"
            class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
            @click="goToDetail(group.groupId)"
            style="cursor: pointer;"
          >
            <div>
              <h6 class="mb-1">{{ group.title }}</h6>
              <small class="text-muted">
                <!-- SplitGroupSummaryResponse는 status 필드 사용 -->
                <span class="badge" :class="getStatusBadgeClass(group.status)">
                  {{ getStatusText(group.status) }}
                </span>
                <!-- SummaryResponse에 pickupLocation 없음 - 제거 -->
                <span v-if="group.isHost" class="ms-2 badge bg-warning text-dark">방장</span>
              </small>
            </div>
            <div class="text-end">
              <span class="badge bg-primary">{{ group.currentParticipants }}/{{ group.maxParticipants }}</span>
              <!--
                채팅 버튼: 팝업 창으로 열기
                @click.stop: 부모 요소(li)의 클릭 이벤트 전파 방지
                openChatPopup(): window.open()으로 새 창 열기
              -->
              <button
                class="btn btn-sm btn-outline-secondary ms-2"
                @click.stop="openChatPopup(group.groupId)"
              >
                채팅
              </button>
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

    /**
     * 채팅방을 팝업 창으로 열기
     * @param {number} groupId - 그룹 ID
     */
    openChatPopup(groupId) {
      // 팝업 창 크기 및 위치 설정
      const width = 420
      const height = 650
      // 화면 오른쪽에 위치
      const left = window.screen.width - width - 20
      const top = 100

      // window.open(URL, 창이름, 옵션)
      // 창 이름을 groupId로 해서 같은 그룹 채팅은 한 창에서만 열리도록
      window.open(
        `/chat/${groupId}`,
        `chat_${groupId}`,
        `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`
      )
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
