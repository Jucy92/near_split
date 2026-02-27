<!--
  파일: PaymentHistoryView.vue
  설명: 결제 내역 조회 페이지
        - 내 결제 목록 조회 (GET /api/payments/my)
        - 결제 상세 정보 펼치기/접기
        - 결제 취소 기능 (POST /api/payments/{paymentKey}/cancel)

  ==================== 페이지 접근 흐름 ====================
  1. NavBar 사용자 드롭다운에서 "결제 내역" 클릭
  2. router가 /payments로 이동
  3. PaymentHistoryView 렌더링
  4. mounted()에서 loadPayments() 호출 → GET /api/payments/my
  5. 결제 목록 표시
  6. "상세보기" 클릭 → 상세 정보 펼침
  7. "결제 취소" 클릭 → 취소 모달 → POST /api/payments/{paymentKey}/cancel

  ==================== API 목록 ====================
  | 기능         | 메서드 | 엔드포인트                    | 호출 함수        |
  |--------------|--------|-------------------------------|------------------|
  | 결제 내역    | GET    | /api/payments/my              | getMyPayments()  |
  | 결제 상세    | GET    | /api/payments/{paymentKey}    | getPayment()     |
  | 결제 취소    | POST   | /api/payments/{paymentKey}/cancel | cancelPayment() |

  ==================== 백엔드 응답 구조 ====================
  GET /api/payments/my 응답 (PaymentResponse[]):
  [
    {
      "id": 1,
      "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw...",
      "orderId": "GROUP_5_1706123456789_abc123",
      "amount": 15000,
      "orderName": "공동구매 참여",
      "method": "카드",
      "status": "DONE",
      "groupId": 5,
      "cardCompany": "삼성",
      "cardNumber": "1234-****-****-5678",
      "approvedAt": "2026-02-08T10:30:00",
      "createdAt": "2026-02-08T10:29:00"
    }
  ]
-->
<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="d-flex align-items-center">
        <router-link to="/home" class="btn btn-outline-secondary me-3">&larr; 홈</router-link>
        <h3 class="mb-0">결제 내역</h3>
      </div>
      <!-- 새로고침 버튼 -->
      <button
        class="btn btn-outline-primary"
        @click="loadPayments"
        :disabled="loading"
      >
        <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
        새로고침
      </button>
    </div>

    <!-- 성공 메시지 -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show">
      {{ successMessage }}
      <button type="button" class="btn-close" @click="successMessage = ''"></button>
    </div>

    <!-- 로딩 중 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩중...</span>
      </div>
      <p class="mt-2 text-muted">결제 내역을 불러오는 중...</p>
    </div>

    <!-- 결제 내역 없음 -->
    <div v-else-if="payments.length === 0" class="text-center py-5">
      <div class="text-muted mb-3" style="font-size: 4rem;">
        <i class="bi bi-credit-card"></i>
      </div>
      <h5 class="text-muted">아직 결제 내역이 없습니다</h5>
      <p class="text-muted">공동구매에 참여하고 결제를 진행해보세요!</p>
      <router-link to="/groups" class="btn btn-primary mt-2">
        공동구매 둘러보기
      </router-link>
    </div>

    <!-- 결제 내역 목록 -->
    <div v-else class="row">
      <div class="col-12 col-lg-8 mx-auto">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <strong>총 {{ payments.length }}건</strong>의 결제 내역
          </div>
          <ul class="list-group list-group-flush">
            <li
              v-for="payment in payments"
              :key="payment.paymentKey"
              class="list-group-item"
            >
              <!-- 결제 기본 정보 -->
              <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                  <!-- 주문명 + 상태 배지 -->
                  <div class="d-flex align-items-center mb-1">
                    <h6 class="mb-0 me-2">{{ payment.orderName || '결제' }}</h6>
                    <span :class="getStatusBadgeClass(payment.status)">
                      {{ getStatusText(payment.status) }}
                    </span>
                  </div>
                  <!-- 결제일시 + 결제수단 -->
                  <div class="text-muted small">
                    <span>{{ formatDateTime(payment.approvedAt || payment.createdAt) }}</span>
                    <span v-if="payment.method" class="ms-2">
                      <i class="bi bi-credit-card-2-front"></i> {{ payment.method }}
                    </span>
                    <span v-if="payment.cardNumber" class="ms-1">({{ payment.cardNumber }})</span>
                  </div>
                </div>
                <!-- 결제 금액 -->
                <div class="text-end">
                  <div class="fw-bold fs-5" :class="payment.status === 'CANCELED' ? 'text-muted text-decoration-line-through' : 'text-primary'">
                    {{ formatPrice(payment.amount) }}원
                  </div>
                </div>
              </div>

              <!-- 결제 상세 정보 (펼침) -->
              <div
                v-if="expandedPaymentKey === payment.paymentKey"
                class="mt-3 p-3 bg-light rounded"
              >
                <div class="row small">
                  <div class="col-md-6 mb-2">
                    <div class="text-muted">주문번호</div>
                    <div class="text-break fw-medium">{{ payment.orderId }}</div>
                  </div>
                  <div class="col-md-6 mb-2">
                    <div class="text-muted">결제키</div>
                    <div class="text-break fw-medium">{{ payment.paymentKey }}</div>
                  </div>
                  <div class="col-md-6 mb-2" v-if="payment.groupId">
                    <div class="text-muted">관련 그룹</div>
                    <div>
                      <router-link
                        :to="`/groups/${payment.groupId}`"
                        class="text-decoration-none"
                      >
                        <i class="bi bi-box-arrow-up-right me-1"></i>
                        그룹 #{{ payment.groupId }} 상세보기
                      </router-link>
                    </div>
                  </div>
                  <div class="col-md-6 mb-2" v-if="payment.cardCompany">
                    <div class="text-muted">카드사</div>
                    <div class="fw-medium">{{ payment.cardCompany }}</div>
                  </div>
                  <div class="col-md-6 mb-2">
                    <div class="text-muted">결제 상태</div>
                    <div class="fw-medium">{{ getStatusText(payment.status) }}</div>
                  </div>
                  <div class="col-md-6 mb-2">
                    <div class="text-muted">결제 승인 시각</div>
                    <div class="fw-medium">{{ formatDateTime(payment.approvedAt) || '-' }}</div>
                  </div>
                </div>
              </div>

              <!-- 버튼 영역 -->
              <div class="mt-3 d-flex gap-2">
                <!-- 상세보기/접기 버튼 -->
                <button
                  class="btn btn-sm btn-outline-secondary"
                  @click="toggleDetail(payment.paymentKey)"
                >
                  <i :class="expandedPaymentKey === payment.paymentKey ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                  {{ expandedPaymentKey === payment.paymentKey ? '접기' : '상세보기' }}
                </button>

                <!-- 결제 취소 버튼 (DONE 상태일 때만) -->
                <button
                  v-if="payment.status === 'DONE'"
                  class="btn btn-sm btn-outline-danger"
                  @click="openCancelModal(payment)"
                >
                  <i class="bi bi-x-circle me-1"></i>결제 취소
                </button>

                <!-- 취소됨 표시 -->
                <span
                  v-else-if="payment.status === 'CANCELED'"
                  class="text-muted small align-self-center"
                >
                  <i class="bi bi-x-circle me-1"></i>취소된 결제입니다
                </span>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- ==================== 결제 취소 모달 ==================== -->
    <div
      v-if="showCancelModal"
      class="modal d-block"
      tabindex="-1"
      style="background-color: rgba(0,0,0,0.5);"
      @click.self="closeCancelModal"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header border-0">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle text-warning me-2"></i>
              결제 취소
            </h5>
            <button type="button" class="btn-close" @click="closeCancelModal"></button>
          </div>
          <div class="modal-body">
            <!-- 취소할 결제 정보 -->
            <div class="alert alert-light border mb-3">
              <div class="fw-bold">{{ selectedPayment?.orderName }}</div>
              <div class="text-muted small mt-1">
                {{ formatDateTime(selectedPayment?.approvedAt) }}
              </div>
              <div class="fs-5 text-primary mt-2">
                {{ formatPrice(selectedPayment?.amount) }}원
              </div>
            </div>

            <div class="alert alert-warning py-2 small">
              <i class="bi bi-info-circle me-1"></i>
              결제 취소 시 환불까지 영업일 기준 3~5일이 소요될 수 있습니다.
            </div>

            <!-- 취소 사유 입력 -->
            <div class="mb-3">
              <label class="form-label fw-medium">
                취소 사유 <span class="text-danger">*</span>
              </label>
              <textarea
                class="form-control"
                v-model="cancelReason"
                rows="3"
                placeholder="취소 사유를 입력해주세요 (예: 단순 변심, 그룹 탈퇴 등)"
                required
              ></textarea>
            </div>

            <!-- 에러 메시지 -->
            <div v-if="cancelError" class="alert alert-danger py-2">
              <i class="bi bi-exclamation-circle me-1"></i>
              {{ cancelError }}
            </div>
          </div>
          <div class="modal-footer border-0">
            <button type="button" class="btn btn-secondary" @click="closeCancelModal">
              닫기
            </button>
            <button
              type="button"
              class="btn btn-danger"
              @click="handleCancelPayment"
              :disabled="!cancelReason.trim() || canceling"
            >
              <span v-if="canceling" class="spinner-border spinner-border-sm me-1"></span>
              {{ canceling ? '취소 처리 중...' : '취소 요청' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 결제 관련 API import
import { getMyPayments, cancelPayment } from '../api/payment'

export default {
  name: 'PaymentHistoryView',

  data() {
    return {
      // 결제 내역 목록
      payments: [],
      // 로딩 상태
      loading: true,
      // 펼쳐진 결제 상세 (paymentKey)
      expandedPaymentKey: null,
      // 성공 메시지
      successMessage: '',

      // ========== 결제 취소 모달 관련 ==========
      showCancelModal: false,      // 모달 표시 여부
      selectedPayment: null,       // 취소할 결제 정보
      cancelReason: '',            // 취소 사유
      canceling: false,            // 취소 처리 중
      cancelError: ''              // 취소 에러 메시지
    }
  },

  async mounted() {
    // 결제 내역 로드
    await this.loadPayments()
  },

  methods: {
    /**
     * 결제 내역 로드
     * GET /api/payments/my 호출
     */
    async loadPayments() {
      this.loading = true
      try {
        const response = await getMyPayments()
        // PaymentController는 ApiResponse 없이 직접 List 반환
        this.payments = response.data || []
        console.log('결제 내역 로드 완료:', this.payments.length, '건')
      } catch (error) {
        console.error('결제 내역 로드 실패:', error)
        this.payments = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 결제 상세 펼치기/접기
     * @param {string} paymentKey - 결제키
     */
    toggleDetail(paymentKey) {
      if (this.expandedPaymentKey === paymentKey) {
        // 이미 펼쳐진 항목 클릭 → 접기
        this.expandedPaymentKey = null
      } else {
        // 다른 항목 펼치기
        this.expandedPaymentKey = paymentKey
      }
    },

    /**
     * 결제 취소 모달 열기
     * @param {Object} payment - 취소할 결제 정보
     */
    openCancelModal(payment) {
      this.selectedPayment = payment
      this.cancelReason = ''
      this.cancelError = ''
      this.showCancelModal = true
    },

    /**
     * 결제 취소 모달 닫기
     */
    closeCancelModal() {
      this.showCancelModal = false
      this.selectedPayment = null
      this.cancelReason = ''
      this.cancelError = ''
    },

    /**
     * 결제 취소 처리
     * POST /api/payments/{paymentKey}/cancel 호출
     */
    async handleCancelPayment() {
      // 취소 사유 검증
      if (!this.cancelReason.trim()) {
        this.cancelError = '취소 사유를 입력해주세요.'
        return
      }

      this.canceling = true
      this.cancelError = ''

      try {
        // 결제 취소 API 호출
        await cancelPayment(this.selectedPayment.paymentKey, this.cancelReason)

        // 성공 시 모달 닫기
        this.closeCancelModal()

        // 성공 메시지 표시
        this.successMessage = '결제가 성공적으로 취소되었습니다.'

        // 결제 내역 새로고침
        await this.loadPayments()

        // 5초 후 성공 메시지 제거
        setTimeout(() => {
          this.successMessage = ''
        }, 5000)

      } catch (error) {
        console.error('결제 취소 실패:', error)
        // 에러 메시지 추출
        if (error.response?.data?.message) {
          this.cancelError = error.response.data.message
        } else {
          this.cancelError = '결제 취소에 실패했습니다. 잠시 후 다시 시도해주세요.'
        }
      } finally {
        this.canceling = false
      }
    },

    // ==================== 헬퍼 메서드 ====================

    /**
     * 결제 상태 텍스트 변환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 한글 상태 텍스트
     */
    getStatusText(status) {
      const statusMap = {
        'READY': '결제 대기',
        'IN_PROGRESS': '결제 진행중',
        'DONE': '결제 완료',
        'CANCELED': '결제 취소',
        'PARTIAL_CANCELED': '부분 취소',
        'ABORTED': '결제 실패',
        'EXPIRED': '결제 만료'
      }
      return statusMap[status] || status
    },

    /**
     * 결제 상태에 따른 배지 클래스
     * @param {string} status - 결제 상태 코드
     * @returns {string} Bootstrap 배지 클래스
     */
    getStatusBadgeClass(status) {
      const classMap = {
        'READY': 'badge bg-secondary',
        'IN_PROGRESS': 'badge bg-warning text-dark',
        'DONE': 'badge bg-success',
        'CANCELED': 'badge bg-danger',
        'PARTIAL_CANCELED': 'badge bg-warning text-dark',
        'ABORTED': 'badge bg-danger',
        'EXPIRED': 'badge bg-secondary'
      }
      return classMap[status] || 'badge bg-secondary'
    },

    /**
     * 날짜/시간 포맷팅
     * @param {string} dateTime - ISO 날짜 문자열
     * @returns {string} 포맷된 날짜/시간
     */
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      const date = new Date(dateTime)
      return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    /**
     * 가격 포맷팅 (천 단위 콤마)
     * @param {number} price - 금액
     * @returns {string} 포맷된 금액
     */
    formatPrice(price) {
      if (!price) return '0'
      return price.toLocaleString('ko-KR')
    }
  }
}
</script>

<style scoped>
/* 목록 아이템 호버 효과 */
.list-group-item {
  transition: background-color 0.15s ease;
}
.list-group-item:hover {
  background-color: #f8f9fa;
}

/* 모달 오버레이 애니메이션 */
.modal {
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
