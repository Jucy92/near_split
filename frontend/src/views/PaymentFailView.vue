<!--
  파일: PaymentFailView.vue
  설명: 결제 실패 시 리다이렉트되는 페이지
        - 토스페이먼츠에서 결제 실패/취소 시 이 페이지로 리다이렉트
        - URL 쿼리 파라미터에서 에러 코드와 메시지 추출
        - 사용자에게 실패 사유 안내 및 재시도 버튼 제공
-->
<template>
  <div class="payment-fail-container">
    <!-- 실패 아이콘 -->
    <div class="fail-icon">✕</div>

    <!-- 메인 메시지 -->
    <h1>결제에 실패했습니다</h1>

    <!-- 에러 상세 정보 -->
    <div class="error-detail">
      <div class="error-code" v-if="errorCode">
        에러 코드: {{ errorCode }}
      </div>
      <p class="error-message">
        {{ errorMessage || '알 수 없는 오류가 발생했습니다.' }}
      </p>
    </div>

    <!-- 안내 메시지 -->
    <div class="help-text">
      <p v-if="errorCode === 'PAY_PROCESS_CANCELED'">
        결제가 취소되었습니다. 다시 시도해 주세요.
      </p>
      <p v-else-if="errorCode === 'REJECT_CARD_COMPANY'">
        카드사에서 결제를 거절했습니다. 다른 카드로 시도해 주세요.
      </p>
      <p v-else>
        잠시 후 다시 시도하거나, 다른 결제수단을 이용해 주세요.
      </p>
    </div>

    <!-- 버튼 그룹 -->
    <div class="button-group">
      <button class="primary-button" @click="retryPayment">
        다시 결제하기
      </button>
      <button class="secondary-button" @click="goToGroup">
        그룹으로 돌아가기
      </button>
      <button class="text-button" @click="goToHome">
        홈으로
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PaymentFailView',

  data() {
    return {
      // URL 쿼리에서 추출한 에러 정보
      errorCode: null,
      errorMessage: null,
      orderId: null,
      groupId: null
    }
  },

  mounted() {
    // URL 쿼리 파라미터에서 에러 정보 추출
    this.extractErrorInfo()
  },

  methods: {
    /**
     * URL 쿼리 파라미터에서 에러 정보 추출
     * 토스페이먼츠가 failUrl로 리다이렉트할 때 아래 파라미터를 전달함:
     * - code: 에러 코드
     * - message: 에러 메시지
     * - orderId: 주문 ID (선택적)
     */
    extractErrorInfo() {
      const query = this.$route.query

      this.errorCode = query.code
      this.errorMessage = query.message
      this.orderId = query.orderId
      this.groupId = query.groupId

      console.log('결제 실패 정보:', {
        errorCode: this.errorCode,
        errorMessage: this.errorMessage,
        orderId: this.orderId,
        groupId: this.groupId
      })
    },

    /**
     * 다시 결제하기
     * - 결제 페이지로 이동
     */
    retryPayment() {
      if (this.groupId) {
        this.$router.push(`/checkout/${this.groupId}`)
      } else {
        this.$router.push('/groups')
      }
    },

    /**
     * 그룹으로 돌아가기
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
    }
  }
}
</script>

<style scoped>
/* 컨테이너 */
.payment-fail-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 60px 20px;
  text-align: center;
}

/* 실패 아이콘 */
.fail-icon {
  width: 80px;
  height: 80px;
  background-color: #ff5252;
  color: white;
  font-size: 40px;
  line-height: 80px;
  border-radius: 50%;
  margin: 0 auto 24px;
}

/* 제목 */
h1 {
  color: #333;
  font-size: 24px;
  margin-bottom: 24px;
}

/* 에러 상세 */
.error-detail {
  background: #fff5f5;
  border: 1px solid #ffcdd2;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.error-code {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.error-message {
  color: #c62828;
  font-size: 16px;
  margin: 0;
}

/* 안내 텍스트 */
.help-text {
  color: #666;
  margin-bottom: 30px;
}

.help-text p {
  margin: 0;
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
  color: #333;
  background-color: #f0f0f0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.secondary-button:hover {
  background-color: #e0e0e0;
}

.text-button {
  padding: 12px;
  font-size: 14px;
  color: #666;
  background: transparent;
  border: none;
  cursor: pointer;
}

.text-button:hover {
  color: #333;
  text-decoration: underline;
}
</style>
