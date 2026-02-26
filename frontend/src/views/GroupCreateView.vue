<!--
  파일: GroupCreateView.vue
  설명: 소분 그룹 생성 페이지
        - 그룹 정보 입력 폼 (제목, 금액, 인원, 장소, 마감일)
        - 1인당 예상 금액 실시간 계산
        - 그룹 생성 후 상세 페이지로 이동
        - 에러 처리: Validation 필드별 에러 + BusinessException 메시지 표시

  ==================== 페이지 접근 흐름 ====================
  1. GroupListView 또는 HomeView에서 "그룹 생성" 버튼 클릭
  2. router가 /groups/new로 이동
  3. GroupCreateView 렌더링
  4. 사용자가 정보 입력 (computed로 1인당 금액 실시간 계산)
  5. "그룹 생성" 버튼 클릭 → handleCreate()
  6. POST /api/split 호출
  7. 성공 시 생성된 그룹의 상세 페이지로 이동 (/groups/{id})

  ==================== API 목록 ====================
  | 기능       | 메서드 | 엔드포인트 | 호출 함수     |
  |------------|--------|------------|---------------|
  | 그룹 생성  | POST   | /api/split | createGroup() |

  ==================== 백엔드 요청/응답 구조 ====================
  POST /api/split
  Request Body (SplitGroupRequest):
  {
    "title": "코스트코 영양제 모집",
    "totalPrice": 150000,
    "maxParticipants": 20,
    "pickupLocation": "강남역 2번 출구",
    "closedAt": "2026-02-15"
  }

  Response (SplitGroupResponse):
  {
    "id": 1,
    "hostUserId": 100,
    "title": "코스트코 영양제 모집",
    "totalPrice": 150000,
    "maxParticipants": 20,
    "currentParticipants": 1,
    "groupState": "RECRUITING",
    ...
  }

  ==================== 에러 응답 구조 ====================
  [1] Validation 에러 (@Valid 실패):
  {
    "code": "C001",
    "message": "잘못된 입력입니다",
    "errors": {                              ← 필드별 상세 에러 맵
      "title": "must not be blank",
      "maxParticipants": "must be greater than or equal to 2"
    }
  }

  [2] BusinessException (G002 등):
  {
    "code": "G002",
    "message": "그룹이 가득 찼습니다"       ← errors 없이 message만
  }

  ==================== 1인당 금액 계산 ====================
  - computed: pricePerPerson
  - 공식: Math.ceil(totalPrice / maxParticipants)
  - 방장 포함 여부는 백엔드 정책에 따름 (현재 방장 포함 N+1빵)
-->
<template>
  <div class="container py-4">
    <!-- 상단 헤더: 목록 링크 + 페이지 제목 -->
    <div class="d-flex align-items-center mb-4">
      <router-link to="/groups" class="btn btn-outline-secondary me-3">&larr; 목록</router-link>
      <h3 class="mb-0">소분 그룹 생성</h3>
    </div>

    <!-- 생성 폼 -->
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-body p-4">
            <!-- 에러 메시지 영역 -->
            <!-- errorMessages 배열에 항목이 있을 때만 표시 -->
            <div v-if="errorMessages.length > 0" class="alert alert-danger">
              <!-- 에러가 여러 개면 목록으로, 하나면 단순 텍스트로 표시 -->
              <ul v-if="errorMessages.length > 1" class="mb-0 ps-3">
                <li v-for="msg in errorMessages" :key="msg">{{ msg }}</li>
              </ul>
              <span v-else>{{ errorMessages[0] }}</span>
            </div>

            <form @submit.prevent="handleCreate">
              <!-- 제목 -->
              <div class="mb-3">
                <label for="title" class="form-label">그룹 제목 *</label>
                <input
                  type="text"
                  class="form-control"
                  id="title"
                  v-model="form.title"
                  placeholder="예: 코스트코 영양제 20인분 모집"
                  required
                />
              </div>

              <!-- 총 금액 -->
              <div class="mb-3">
                <label for="totalPrice" class="form-label">총 금액 (원) *</label>
                <input
                  type="number"
                  class="form-control"
                  id="totalPrice"
                  v-model.number="form.totalPrice"
                  placeholder="150000"
                  min="0"
                  required
                />
              </div>

              <!-- 최대 참여자 수 -->
              <div class="mb-3">
                <label for="maxParticipants" class="form-label">최대 참여자 수 *</label>
                <input
                  type="number"
                  class="form-control"
                  id="maxParticipants"
                  v-model.number="form.maxParticipants"
                  placeholder="20"
                  min="2"
                  max="100"
                  required
                />
                <div class="form-text">1인당 예상 금액: {{ pricePerPerson }}원</div>
              </div>

              <!-- 픽업 장소 -->
              <div class="mb-3">
                <label for="pickupLocation" class="form-label">픽업 장소</label>
                <input
                  type="text"
                  class="form-control"
                  id="pickupLocation"
                  v-model="form.pickupLocation"
                  placeholder="강남역 2번 출구"
                />
              </div>

              <!-- 모집 마감일 -->
              <div class="mb-4">
                <label for="closedAt" class="form-label">모집 마감일</label>
                <input
                  type="date"
                  class="form-control"
                  id="closedAt"
                  v-model="form.closedAt"
                  :min="minDate"
                />
              </div>

              <!-- 버튼 -->
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary flex-grow-1" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  {{ loading ? '생성 중...' : '그룹 생성' }}
                </button>
                <router-link to="/groups" class="btn btn-outline-secondary">취소</router-link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { createGroup } from '../api/group'

// ==================== 필드명 → 한국어 라벨 매핑 ====================
// 백엔드 SplitGroupRequest의 필드명을 화면에 표시할 라벨로 변환
// 에러 메시지에 "title" 같은 변수명이 노출되지 않도록 함
const FIELD_LABELS = {
  title: '그룹 제목',
  totalPrice: '총 금액',
  maxParticipants: '최대 참여자 수',
  pickupLocation: '픽업 장소',
  closedAt: '모집 마감일',
  pickupLocationGeo: '픽업 위치 좌표'
}

// ==================== Spring Validation 메시지 → 한국어 매핑 ====================
// @NotBlank, @NotNull, @Positive, @Min 등이 반환하는 기본 영문 메시지를 한국어로 변환
const VALIDATION_MESSAGES = {
  'must not be blank': '필수 입력 항목입니다',
  'must not be null': '필수 입력 항목입니다',
  'must be greater than 0': '0보다 큰 값을 입력해주세요',
  'must be a positive number': '양수를 입력해주세요',
  'must be greater than or equal to 2': '최소 2명 이상이어야 합니다'
}

export default {
  name: 'GroupCreateView',

  data() {
    return {
      form: {
        title: '',
        totalPrice: null,
        maxParticipants: null,
        pickupLocation: '',
        closedAt: ''
      },
      loading: false,
      errorMessages: []   // 단일 문자열 대신 배열로 관리 (필드별 에러 여러 개 가능)
    }
  },

  computed: {
    pricePerPerson() {
      if (this.form.totalPrice && this.form.maxParticipants) {
        return Math.ceil(this.form.totalPrice / this.form.maxParticipants).toLocaleString()
      }
      return '0'
    },
    minDate() {
      // 오늘 날짜를 YYYY-MM-DD 형식으로
      const today = new Date()
      return today.toISOString().split('T')[0]
    }
  },

  methods: {
    async handleCreate() {
      this.loading = true
      this.errorMessages = []   // 이전 에러 초기화

      try {
        const response = await createGroup(this.form)
        const createdGroup = response.data
        // 생성된 그룹 상세 페이지로 이동
        this.$router.push(`/groups/${createdGroup.id}`)
      } catch (error) {
        console.error('그룹 생성 실패:', error)
        const data = error.response?.data

        if (data?.errors) {
          // [케이스 1] Validation 에러: errors 맵에 필드별 상세 에러가 담겨 있음
          // Object.entries()로 { 필드명: 에러메시지 } 쌍을 배열로 변환
          this.errorMessages = Object.entries(data.errors).map(([field, msg]) => {
            // 필드명을 한국어 라벨로 변환 (매핑 없으면 원래 필드명 그대로 사용)
            const label = FIELD_LABELS[field] || field
            // 영문 Validation 메시지를 한국어로 변환 (매핑 없으면 원래 메시지 그대로)
            const message = VALIDATION_MESSAGES[msg] || msg
            return `${label}: ${message}`
          })
        } else if (data?.message) {
          // [케이스 2] BusinessException: 단일 한국어 메시지만 있음 (G002 등)
          this.errorMessages = [data.message]
        } else {
          // [케이스 3] 네트워크 오류 등 예상치 못한 에러
          this.errorMessages = ['그룹 생성에 실패했습니다.']
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
