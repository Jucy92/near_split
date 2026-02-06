<!--
  파일: HomeView.vue
  설명: 로그인 후 첫 화면 (대시보드)
        - 사용자 환영 메시지 표시
        - 주요 기능으로 이동하는 메뉴 카드들
        - 참여 그룹 현황 요약 (→ /groups?tab=my로 이동)
        - 최근 모집 중인 그룹 목록

  ==================== 페이지 접근 흐름 ====================
  1. 사용자가 /login에서 로그인 성공
  2. router가 '/'(홈)으로 리다이렉트
  3. router/index.js의 beforeEach 가드에서 isLoggedIn 체크
     - localStorage에 'isLoggedIn'이 없으면 /login으로 리다이렉트
     - 있으면 HomeView 렌더링
  4. mounted()에서 loadData() 호출 → 3개 API 병렬 요청
  5. 화면에 데이터 표시

  ==================== 데이터 흐름 ====================
  mounted() → loadData() → 3개 API 병렬 호출:
    - getMyProfile()    → GET /api/users/me       → this.user
    - getMyGroups()     → GET /api/split/my       → this.myGroupCount
    - getGroups(0, 5)   → GET /api/split?page=0&size=5 → this.recentGroups

  ==================== API 목록 ====================
  | 기능             | 메서드 | 엔드포인트                | 호출 함수        |
  |------------------|--------|---------------------------|------------------|
  | 내 프로필 조회   | GET    | /api/users/me             | getMyProfile()   |
  | 내 그룹 목록     | GET    | /api/split/my             | getMyGroups()    |
  | 그룹 목록 (페이징)| GET   | /api/split?page=0&size=5  | getGroups(0, 5)  |
  | 로그아웃         | POST   | /api/auth/logout          | logout()         |

  ==================== 컴포넌트 구조 ====================
  HomeView
  ├── 상단 헤더 (프로필 링크 + 로그아웃 버튼)
  ├── 환영 메시지 (user 데이터 사용)
  ├── 메뉴 카드들
  │   ├── 소분 그룹 카드 (→ /groups, /groups/new)
  │   ├── 상품 관리 카드 (→ /products, /products/new)
  │   ├── 참여 그룹 카드 (→ /groups?tab=my, myGroupCount 표시)
  │   └── 프로필 카드 (→ /profile)
  └── 최근 그룹 목록 (recentGroups 배열 v-for)
-->
<template>
  <div class="container py-4">
    <!-- 상단 헤더: 로고 + 프로필/로그아웃 버튼 -->
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

      <!--
        참여 그룹 요약 카드
        - "내 그룹" → "참여 그룹"으로 변경
        - 클릭 시 /groups?tab=my로 이동 → GroupListView에서 탭 없이 참여 그룹만 표시
      -->
      <div class="col-12 col-md-6">
        <div class="card h-100 shadow-sm">
          <div class="card-body">
            <h5 class="card-title">
              <i class="bi bi-bookmark-star text-warning me-2"></i>
              참여 그룹
            </h5>
            <p class="card-text text-muted">내가 참여 중인 그룹을 확인하세요.</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge bg-warning text-dark fs-6">{{ myGroupCount }}개 참여 중</span>
              <router-link to="/groups?tab=my" class="btn btn-outline-warning btn-sm">보러가기</router-link>
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
// ===========================
// API 함수 import
// ===========================
// logout: POST /api/auth/logout (쿠키의 JWT 토큰 삭제)
import { logout } from '../api/auth'
// getMyProfile: GET /api/users/me (현재 로그인한 사용자 정보)
import { getMyProfile } from '../api/user'
// getGroups: GET /api/split (전체 그룹 목록, 페이징)
// getMyGroups: GET /api/split/my (내가 참여 중인 그룹)
import { getGroups, getMyGroups } from '../api/group'

export default {
  name: 'HomeView',

  // ===========================
  // 컴포넌트 상태 (data)
  // ===========================
  data() {
    return {
      // user: 현재 로그인한 사용자 정보 (GET /api/users/me 응답)
      // 구조: { id, email, name, nickname, address, phone }
      user: null,

      // myGroupCount: 내가 참여 중인 그룹 수
      // GET /api/split/my 응답 배열의 length
      myGroupCount: 0,

      // recentGroups: 최근 모집 중인 그룹 목록 (최대 5개)
      // GET /api/split?page=0&size=5 응답의 content 배열
      // 각 항목 구조: { id, title, pickupLocation, currentParticipants, maxParticipants, ... }
      recentGroups: [],

      // loading: 데이터 로딩 중 여부 (로딩 스피너 표시용)
      loading: false
    }
  },

  // ===========================
  // 라이프사이클 훅
  // ===========================
  // mounted: 컴포넌트가 DOM에 마운트된 직후 실행
  // 이 시점에 API 호출하여 화면에 필요한 데이터 로드
  async mounted() {
    await this.loadData()
  },

  methods: {
    // ===========================
    // 데이터 로드 (초기 로딩)
    // ===========================
    // 3개의 API를 Promise.all로 병렬 호출하여 성능 최적화
    // 순차 호출 시: API1(200ms) → API2(200ms) → API3(200ms) = 총 600ms
    // 병렬 호출 시: API1, API2, API3 동시 → 가장 느린 것 기준 = 약 200ms
    async loadData() {
      this.loading = true
      try {
        // ─────────────────────────────────────────
        // Promise.all: 모든 Promise가 완료될 때까지 대기
        // 하나라도 실패하면 catch 블록으로 이동
        // ─────────────────────────────────────────
        const [profileRes, myGroupsRes, groupsRes] = await Promise.all([
          getMyProfile(),     // GET /api/users/me
          getMyGroups(),      // GET /api/split/my
          getGroups(0, 5)     // GET /api/split?page=0&size=5 (최근 5개)
        ])

        // ─────────────────────────────────────────
        // 응답 데이터 매핑
        // ─────────────────────────────────────────
        // UserController는 ApiResponse로 감싸지 않고 직접 반환하므로 data.data
        this.user = profileRes.data.data

        // myGroups는 배열, 그 길이가 참여 중인 그룹 수
        this.myGroupCount = myGroupsRes.data.data?.length || 0

        // groups는 Page 객체, content 필드에 실제 데이터 배열
        // Spring Page 응답 구조: { content: [...], totalPages, totalElements, ... }
        this.recentGroups = groupsRes.data.data?.content || []
      } catch (error) {
        // 에러 발생 시 콘솔에 로그 (axios 인터셉터에서 전역 처리됨)
        console.error('데이터 로드 실패:', error)
      } finally {
        // 성공/실패 관계없이 로딩 상태 해제
        this.loading = false
      }
    },

    // ===========================
    // 로그아웃 처리
    // ===========================
    // 1. 백엔드에 로그아웃 요청 (쿠키 삭제)
    // 2. localStorage에서 로그인 상태 제거
    // 3. 로그인 페이지로 이동
    async handleLogout() {
      try {
        // POST /api/auth/logout
        // 백엔드에서 accessToken, refreshToken 쿠키를 maxAge=0으로 설정하여 삭제
        await logout()

        // 프론트엔드 로그인 상태 제거
        // router guard에서 이 값으로 인증 여부 판단
        localStorage.removeItem('isLoggedIn')

        // 로그인 페이지로 이동
        this.$router.push('/login')
      } catch (error) {
        // 로그아웃 실패해도 로그인 페이지로 이동 (쿠키 만료 등의 이유)
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
