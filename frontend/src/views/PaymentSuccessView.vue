<!--
  파일: PaymentSuccessView.vue
  설명: 결제 성공 후 리다이렉트되는 페이지
        - 토스페이먼츠에서 결제 인증 완료 후 이 페이지로 리다이렉트
        - URL 쿼리 파라미터에서 paymentKey, orderId, amount 추출
        - 백엔드로 결제 승인 요청 → 토스페이먼츠 결제 승인 API 호출
-->
<template>
  <div class="payment-success-container">
    <!-- 처리 중 -->
    <div v-if="processing" class="processing">
      <div class="spinner"></div>
      <h2>결제를 처리하고 있습니다...</h2>
      <p>잠시만 기다려 주세요.</p>
    </div>

    <!-- 결제 완료 -->
    <div v-else-if="success" class="success">
      <div class="success-icon">✓</div>
      <h1>결제가 완료되었습니다!</h1>

      <div class="payment-info">
        <div class="info-row">
          <span class="label">주문번호</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="info-row">
          <span class="label">결제금액</span>
          <span class="value price">{{ formatPrice(amount) }}원</span>
        </div>
        <div class="info-row" v-if="paymentMethod">
          <span class="label">결제수단</span>
          <span class="value">{{ paymentMethod }}</span>
        </div>
      </div>

      <div class="button-group">
        <button class="primary-button" @click="goToGroup">
          그룹으로 돌아가기
        </button>
        <button class="secondary-button" @click="goToHome">
          홈으로
        </button>
      </div>
    </div>

    <!-- 결제 승인 실패 -->
    <div v-else-if="error" class="error">
      <div class="error-icon">!</div>
      <h1>결제 승인에 실패했습니다</h1>
      <p class="error-message">{{ error }}</p>

      <div class="button-group">
        <button class="primary-button" @click="retryPayment">
          다시 결제하기
        </button>
        <button class="secondary-button" @click="goToHome">
          홈으로
        </button>
      </div>
    </div>
  </div>
</template>

<script>
// 결제 승인 API
import { confirmPayment } from '@/api/payment'

export default {
  name: 'PaymentSuccessView',

  data() {
    return {
      // 처리 상태
      processing: true,
      success: false,
      error: null,

      // 결제 정보 (URL 쿼리에서 추출)
      paymentKey: null,
      orderId: null,
      amount: null,
      groupId: null,

      // 결제 결과 정보
      paymentMethod: null
    }
  },

  async mounted() {
    // 1. URL 쿼리 파라미터에서 결제 정보 추출
    this.extractPaymentInfo()

    // 2. 백엔드로 결제 승인 요청
    await this.confirmPayment()
  },

  methods: {
    /**
     * URL 쿼리 파라미터에서 결제 정보 추출
     * 토스페이먼츠가 successUrl로 리다이렉트할 때 아래 파라미터를 전달함:
     * - paymentKey: 결제 키 (토스페이먼츠 발급)
     * - orderId: 주문 ID (우리가 생성한 값)
     * - amount: 결제 금액
     * - paymentType: 결제 유형 (NORMAL, BRANDPAY 등)
     */
    extractPaymentInfo() {
      const query = this.$route.query

      this.paymentKey = query.paymentKey
      this.orderId = query.orderId
      this.amount = parseInt(query.amount, 10)
      this.groupId = query.groupId

      console.log('결제 정보:', {
        paymentKey: this.paymentKey,
        orderId: this.orderId,
        amount: this.amount,
        groupId: this.groupId
      })
    },

    /**
     * 백엔드로 결제 승인 요청
     * - 백엔드가 토스페이먼츠 결제 승인 API 호출
     * - 결제 정보를 DB에 저장
     */
    async confirmPayment() {
      // 필수 파라미터 검증
      if (!this.paymentKey || !this.orderId || !this.amount) {
        this.error = '결제 정보가 올바르지 않습니다.'
        this.processing = false
        return
      }

      try {
        // 백엔드 결제 승인 API 호출
        // POST /api/payments/confirm
        const response = await confirmPayment(
          this.paymentKey,
          this.orderId,
          this.amount
        )

        console.log('결제 승인 성공:', response.data)

        // 결제 수단 정보 저장 (카드, 계좌이체 등)
        if (response.data.method) {
          this.paymentMethod = response.data.method
        }

        // 성공 상태로 변경
        this.success = true

      } catch (err) {
        console.error('결제 승인 실패:', err)

        // 에러 메시지 추출
        if (err.response?.data?.message) {
          this.error = err.response.data.message
        } else {
          this.error = '결제 승인 중 오류가 발생했습니다.'
        }

      } finally {
        this.processing = false
      }
    },

    /**
     * 그룹 상세 페이지로 이동
     */
    goToGroup() {
      if (this.groupId) {
        this.$router.push(`/groups/${this.groupId}`)
      } else {
        this.$router.push('/groups')
      }
    },

    /**
     * 홈으로 이동
     */
    goToHome() {
      this.$router.push('/groups')
    },

    /**
     * 다시 결제하기
     */
    retryPayment() {
      if (this.groupId) {
        this.$router.push(`/checkout/${this.groupId}`)
      } else {
        this.$router.push('/groups')
      }
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
/* 컨테이너 */
.payment-success-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

/* 처리 중 */
.processing {
  padding: 60px 0;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #eee;
  border-top-color: #0064ff;
  border-radius: 50%;
  margin: 0 auto 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.processing h2 {
  color: #333;
  margin-bottom: 10px;
}

.processing p {
  color: #666;
}

/* 성공 */
.success-icon {
  width: 80px;
  height: 80px;
  background-color: #00c853;
  color: white;
  font-size: 40px;
  line-height: 80px;
  border-radius: 50%;
  margin: 0 auto 20px;
}

.success h1 {
  color: #333;
  margin-bottom: 30px;
}

/* 결제 정보 */
.payment-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
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
  font-weight: 700;
}

/* 에러 */
.error-icon {
  width: 80px;
  height: 80px;
  background-color: #ff5252;
  color: white;
  font-size: 40px;
  line-height: 80px;
  border-radius: 50%;
  margin: 0 auto 20px;
}

.error h1 {
  color: #333;
  margin-bottom: 15px;
}

.error .error-message {
  color: #ff5252;
  margin-bottom: 30px;
}

/* 버튼 그룹 */
.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.primary-button {
  width: 100%;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background-color: #0064ff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.primary-button:hover {
  background-color: #0052d4;
}

.secondary-button {
  width: 100%;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  background-color: #f0f0f0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.secondary-button:hover {
  background-color: #e0e0e0;
}
</style>
