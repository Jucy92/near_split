/**
 * 파일: payment.js
 * 설명: 결제 관련 API 호출 함수
 *       - 토스페이먼츠 결제 승인 요청
 *       - 결제 내역 조회
 *
 * API 엔드포인트:
 * | 기능         | 메서드 | 엔드포인트                    | 설명                    |
 * |-------------|--------|------------------------------|------------------------|
 * | 결제 승인    | POST   | /api/payments/confirm        | 토스페이먼츠 결제 승인   |
 * | 결제 조회    | GET    | /api/payments/{paymentKey}   | 결제 정보 조회          |
 * | 내 결제 목록 | GET    | /api/payments/my             | 내 결제 내역 조회       |
 */
import apiClient from './axios'

/**
 * 결제 승인 API
 * - 토스페이먼츠에서 결제 인증 후 successUrl로 리다이렉트되면 호출
 * - 백엔드가 토스페이먼츠 결제 승인 API를 호출하고 결과 반환
 *
 * @param {string} paymentKey - 토스페이먼츠에서 발급한 결제 키
 * @param {string} orderId - 주문 ID (우리가 생성)
 * @param {number} amount - 결제 금액
 * @param {number} groupId - 그룹 ID (결제한 그룹 식별용)
 * @returns {Promise} - 결제 결과
 */
export const confirmPayment = (paymentKey, orderId, amount, groupId) => {
  // POST /api/payments/confirm
  // Body: { paymentKey, orderId, amount, groupId }
  return apiClient.post('/payments/confirm', {
    paymentKey,
    orderId,
    amount,
    groupId
  })
}

/**
 * 결제 정보 조회 API
 *
 * @param {string} paymentKey - 결제 키
 * @returns {Promise} - 결제 정보
 */
export const getPayment = (paymentKey) => {
  // GET /api/payments/{paymentKey}
  return apiClient.get(`/payments/${paymentKey}`)
}

/**
 * 내 결제 내역 조회 API
 *
 * @returns {Promise} - 결제 내역 목록
 */
export const getMyPayments = () => {
  // GET /api/payments/my
  return apiClient.get('/payments/my')
}

/**
 * 결제 취소 API
 *
 * @param {string} paymentKey - 결제 키
 * @param {string} cancelReason - 취소 사유
 * @returns {Promise} - 취소 결과
 */
export const cancelPayment = (paymentKey, cancelReason) => {
  // POST /api/payments/{paymentKey}/cancel
  return apiClient.post(`/payments/${paymentKey}/cancel`, {
    cancelReason
  })
}
