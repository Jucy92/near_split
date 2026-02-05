<!--
  파일: CheckoutView.vue
  설명: 토스페이먼츠 결제 페이지
        - 결제위젯 SDK로 결제 UI 렌더링
        - 카드, 계좌이체, 간편결제 등 다양한 결제수단 지원
        - 결제 요청 시 토스페이먼츠 결제창 호출
-->
<template>
  <div class="checkout-container">
    <!-- 페이지 헤더 -->
    <h1>결제하기</h1>

    <!-- 주문 정보 영역 -->
    <div class="order-info" v-if="groupInfo">
      <h2>주문 정보</h2>
      <div class="info-row">
        <span class="label">상품명</span>
        <span class="value">{{ groupInfo.title }}</span>
      </div>
      <div class="info-row">
        <span class="label">결제 금액</span>
        <!-- totalPrice / maxParticipants로 1인당 금액 계산 -->
        <span class="value price">{{ formatPrice(pricePerPerson) }}원</span>
      </div>
    </div>

    <!-- 로딩 중 표시 -->
    <div v-if="loading" class="loading">
      결제 정보를 불러오는 중...
    </div>

    <!-- 토스페이먼츠 결제위젯이 렌더링될 영역 -->
    <div id="payment-method"></div>

    <!-- 이용약관 영역 -->
    <div id="agreement"></div>

    <!-- 결제하기 버튼 -->
    <!-- 결제하기 버튼: 1인당 금액으로 결제 -->
    <button
      class="payment-button"
      @click="requestPayment"
      :disabled="!isWidgetReady || paying"
    >
      {{ paying ? '결제 처리 중...' : formatPrice(pricePerPerson) + '원 결제하기' }}
    </button>

    <!-- 에러 메시지 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script>
// 그룹 정보 조회 API (상대 경로 사용)
import { getGroup } from '../api/group'

export default {
  name: 'CheckoutView',

  data() {
    return {
      // 그룹(주문) 정보
      groupInfo: null,
      // 토스페이먼츠 위젯 인스턴스
      widgets: null,
      // 위젯 로딩 완료 여부
      isWidgetReady: false,
      // 페이지 로딩 중
      loading: true,
      // 결제 진행 중
      paying: false,
      // 에러 메시지
      error: null,
      // 토스페이먼츠 클라이언트 키 (테스트)
      clientKey: 'test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm'
    }
  },

  // computed: 계산된 속성 (totalPrice / (maxParticipants + 1))
  computed: {
    /**
     * 1인당 결제 금액 계산
     * - GroupDetailView와 동일한 계산 방식
     * - 방장도 금액을 나누므로 총 인원 = maxParticipants + 1 (방장)
     * - 소수점 올림 (정수 금액)
     */
    pricePerPerson() {
      if (!this.groupInfo) return 0
      const { totalPrice, maxParticipants } = this.groupInfo
      if (!totalPrice || !maxParticipants) return 0
      // 방장 포함 N빵: totalPrice / (maxParticipants + 1)
      return Math.ceil(Number(totalPrice) / (maxParticipants + 1))
    }
  },

  async mounted() {
    // 1. 그룹 정보 조회
    await this.fetchGroupInfo()

    // 2. 토스페이먼츠 SDK 로드 및 결제위젯 초기화
    await this.initTossPayments()
  },

  methods: {
    /**
     * 그룹(주문) 정보 조회
     * - URL 파라미터에서 groupId 추출
     * - 백엔드 API로 그룹 상세 정보 조회
     */
    async fetchGroupInfo() {
      try {
        // URL에서 groupId 추출 (예: /checkout/123 → 123)
        const groupId = this.$route.params.groupId

        // 백엔드 API 호출
        const response = await getGroup(groupId)
        this.groupInfo = response.data

        // 디버깅: 그룹 정보 확인
        console.log('그룹 정보 로드:', this.groupInfo)

      } catch (err) {
        console.error('그룹 정보 조회 실패:', err)
        this.error = '주문 정보를 불러올 수 없습니다.'
      }
    },

    /**
     * 토스페이먼츠 SDK 초기화
     * - 외부 스크립트 동적 로드
     * - 결제위젯 인스턴스 생성 및 렌더링
     */
    async initTossPayments() {
      try {
        // 1. 토스페이먼츠 SDK 스크립트 동적 로드
        await this.loadTossPaymentsScript()

        // 2. TossPayments 객체 초기화
        // window.TossPayments는 SDK 스크립트가 로드되면 전역에 생성됨
        const tossPayments = window.TossPayments(this.clientKey)

        // 3. 결제위젯 인스턴스 생성
        // customerKey: 구매자 고유 식별자 (회원 ID 등)
        // 비회원은 TossPayments.ANONYMOUS 사용
        const customerKey = this.generateCustomerKey()
        this.widgets = tossPayments.widgets({ customerKey })

        // 4. 결제 금액 설정 (1인당 금액 = totalPrice / maxParticipants)
        await this.widgets.setAmount({
          currency: 'KRW',
          value: this.pricePerPerson
        })

        // 5. 결제 UI 렌더링
        // selector: 결제위젯이 렌더링될 DOM 요소의 CSS 선택자
        await this.widgets.renderPaymentMethods({
          selector: '#payment-method',
          variantKey: 'DEFAULT'  // 기본 결제 UI 사용
        })

        // 6. 이용약관 UI 렌더링
        await this.widgets.renderAgreement({
          selector: '#agreement',
          variantKey: 'AGREEMENT'
        })

        // 7. 로딩 완료
        this.isWidgetReady = true
        this.loading = false

      } catch (err) {
        console.error('토스페이먼츠 초기화 실패:', err)
        this.error = '결제 시스템을 불러올 수 없습니다.'
        this.loading = false
      }
    },

    /**
     * 토스페이먼츠 SDK 스크립트 동적 로드
     * - script 태그를 동적으로 생성하여 SDK 로드
     * - 이미 로드되어 있으면 스킵
     */
    loadTossPaymentsScript() {
      return new Promise((resolve, reject) => {
        // 이미 로드되어 있으면 바로 resolve
        if (window.TossPayments) {
          resolve()
          return
        }

        // script 태그 생성
        const script = document.createElement('script')
        script.src = 'https://js.tosspayments.com/v2/standard'
        script.async = true

        // 로드 성공 시
        script.onload = () => resolve()

        // 로드 실패 시
        script.onerror = () => reject(new Error('토스페이먼츠 SDK 로드 실패'))

        // DOM에 script 태그 추가 → 로드 시작
        document.head.appendChild(script)
      })
    },

    /**
     * 결제 요청
     * - 토스페이먼츠 결제창 호출
     * - 성공 시 successUrl로, 실패 시 failUrl로 리다이렉트
     */
    async requestPayment() {
      if (!this.isWidgetReady || this.paying) return

      try {
        this.paying = true
        this.error = null

        // 주문 ID 생성 (고유해야 함)
        const orderId = this.generateOrderId()

        // 결제 요청
        // requestPayment() 호출 시 토스페이먼츠 결제창이 열림
        // 결제 완료 후 successUrl 또는 failUrl로 리다이렉트됨
        await this.widgets.requestPayment({
          // 주문 정보
          orderId: orderId,
          // orderName: 그룹 제목 사용, 없으면 기본값
          orderName: this.groupInfo.title || `공동구매 #${this.groupInfo.id}`,

          // 리다이렉트 URL
          // 결제 성공 시 이동할 URL (쿼리 파라미터로 결제 정보 전달됨)
          successUrl: `${window.location.origin}/payment/success?groupId=${this.groupInfo.id}`,
          // 결제 실패 시 이동할 URL
          failUrl: `${window.location.origin}/payment/fail?groupId=${this.groupInfo.id}`,

          // 구매자 정보 (선택)
          // customerEmail: 'customer@example.com',
          // customerName: '홍길동',
          // customerMobilePhone: '01012345678'
        })

      } catch (err) {
        console.error('결제 요청 실패:', err)
        this.error = err.message || '결제 요청에 실패했습니다.'
        this.paying = false
      }
    },

    /**
     * 고유한 주문 ID 생성
     * - 형식: GROUP_{groupId}_{timestamp}_{random}
     * - 토스페이먼츠 요구사항: 영문, 숫자, -, _ 만 허용
     */
    generateOrderId() {
      const timestamp = Date.now()
      const random = Math.random().toString(36).substring(2, 8)
      return `GROUP_${this.groupInfo.id}_${timestamp}_${random}`
    },

    /**
     * 고객 고유 키 생성
     * - localStorage에 저장하여 재사용
     * - 결제수단 저장 등에 사용됨
     */
    generateCustomerKey() {
      let customerKey = localStorage.getItem('toss_customer_key')
      if (!customerKey) {
        // UUID 형식으로 생성
        customerKey = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
          const r = Math.random() * 16 | 0
          const v = c === 'x' ? r : (r & 0x3 | 0x8)
          return v.toString(16)
        })
        localStorage.setItem('toss_customer_key', customerKey)
      }
      return customerKey
    },

    /**
     * 가격 포맷팅 (천 단위 콤마)
     */
    formatPrice(price) {
      if (!price) return '0'
      return price.toLocaleString('ko-KR')
    }
  }
}
</script>

<style scoped>
/* 결제 페이지 컨테이너 */
.checkout-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

/* 페이지 제목 */
h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

/* 주문 정보 카드 */
.order-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
}

.order-info h2 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: #666;
}

.info-row .value {
  font-weight: 500;
  color: #333;
}

.info-row .value.price {
  color: #0064ff;
  font-size: 18px;
  font-weight: 700;
}

/* 로딩 표시 */
.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

/* 결제위젯 영역 */
#payment-method {
  margin-bottom: 20px;
}

#agreement {
  margin-bottom: 30px;
}

/* 결제하기 버튼 */
.payment-button {
  width: 100%;
  padding: 16px;
  font-size: 18px;
  font-weight: 700;
  color: white;
  background-color: #0064ff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.payment-button:hover:not(:disabled) {
  background-color: #0052d4;
}

.payment-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 에러 메시지 */
.error-message {
  margin-top: 20px;
  padding: 12px;
  background-color: #fee;
  color: #c00;
  border-radius: 8px;
  text-align: center;
}
</style>
