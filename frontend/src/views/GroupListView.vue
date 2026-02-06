<!--
  파일: GroupListView.vue
  설명: 소분 그룹 목록 페이지
        - 전체 그룹 목록 (페이징)
        - 참여 그룹 목록 (내가 참여 중인 그룹)
        - 탭으로 전환 가능
        - HomeView '참여 그룹'에서 진입 시 탭 없이 참여 그룹만 표시 (myOnly 모드)

  ==================== 페이지 접근 흐름 ====================
  1-A. HomeView "그룹 목록" 클릭 → /groups → 전체 그룹/참여 그룹 탭 표시
  1-B. HomeView "참여 그룹" 클릭 → /groups?tab=my → 탭 숨김, 참여 그룹만 표시
  2. router가 /groups로 이동
  3. GroupListView 렌더링
  4. mounted()에서 쿼리 파라미터 확인:
     - tab=my: myOnly=true, 참여 그룹만 로드
     - 없음: 전체 그룹 + 참여 그룹 로드
  5. 그룹 카드 클릭 → goToDetail(groupId) → /groups/{id}로 이동

  ==================== 데이터 흐름 ====================
  mounted() → 쿼리 파라미터에 따라 분기:
    - tab=my (myOnly 모드): loadMyGroups()만 호출 → this.myGroups
    - 그 외 (일반 모드): loadAllGroups() + loadMyGroups() 호출

  탭 전환 시 (watch: currentTab):
    - 'all'  → loadAllGroups()
    - 'my'   → loadMyGroups()

  ==================== API 목록 ====================
  | 기능             | 메서드 | 엔드포인트              | 호출 함수      |
  |------------------|--------|-------------------------|----------------|
  | 전체 그룹 목록   | GET    | /api/split?page=N&size=M| getGroups()    |
  | 참여 그룹 목록   | GET    | /api/split/my           | getMyGroups()  |

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

  2. 참여 그룹 (GET /api/split/my) - SplitGroupSummaryResponse[]
     [
       { "groupId": 1, "status": "RECRUITING", "isHost": true, ... }
     ]
     - groupId 필드 사용 (id 아님!)
     - status 필드 사용 (groupState 아님!)
     - isHost: 방장 여부

  ==================== 컴포넌트 구조 ====================
  GroupListView
  ├── 상단 헤더 (홈 링크 + 그룹 생성 버튼)
  ├── 탭 (전체 그룹 / 참여 그룹) - myOnly 모드에서는 숨김
  ├── 전체 그룹 탭
  │   ├── 그룹 카드 그리드 (groups 배열 v-for)
  │   └── 페이지네이션
  └── 참여 그룹 탭 (또는 myOnly 모드)
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

    <!--
      탭 (전체 / 참여 그룹)
      - myOnly 모드일 때는 탭 자체를 숨김 (HomeView '내 그룹'에서 진입 시)
      - 일반 모드일 때는 전체 그룹 / 참여 그룹 탭 표시
    -->
    <ul class="nav nav-tabs mb-4" v-if="!myOnly">
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
          참여 그룹
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

      <!-- 참여 그룹 (myOnly 모드 또는 '참여 그룹' 탭 선택 시) -->
      <div v-else>
        <div v-if="myGroups.length === 0" class="alert alert-info">
          참여 중인 그룹이 없습니다.
        </div>
        <div v-else class="list-group">
          <!--
            참여 그룹 목록: SplitGroupSummaryResponse 사용
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
      // currentTab: 현재 선택된 탭 ('all' 또는 'my')
      currentTab: 'all',
      // myOnly: HomeView '내 그룹'에서 진입 시 true → 탭 숨기고 참여 그룹만 표시
      myOnly: false,
      loading: false,
      // 전체 그룹
      groups: [],
      currentPage: 0,
      totalPages: 0,
      // 참여 그룹 (내 그룹)
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
    // URL 쿼리 파라미터 확인: /groups?tab=my 형태로 접근 시
    // HomeView '내 그룹' 카드에서 진입하면 tab=my가 붙음
    const tabParam = this.$route.query.tab

    if (tabParam === 'my') {
      // myOnly 모드: 탭 숨기고 참여 그룹만 표시
      this.myOnly = true
      this.currentTab = 'my'
      await this.loadMyGroups()
    } else {
      // 일반 모드: 전체 그룹 + 참여 그룹 탭 표시
      this.myOnly = false
      await this.loadAllGroups()
      await this.loadMyGroups()
    }
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
