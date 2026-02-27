/**
 * 파일: utils/errorParser.js
 * 설명: 백엔드 에러 응답을 화면 표시용 메시지 배열로 변환하는 공통 유틸
 *       - Validation 에러 (@Valid 실패): errors 맵의 필드별 에러를 한국어로 변환
 *       - BusinessException: 단일 message를 그대로 반환
 *       - 네트워크 에러: defaultMessage 반환
 *
 * ==================== 백엔드 에러 응답 구조 ====================
 * [1] Validation 에러:
 * { "code": "C001", "message": "잘못된 입력입니다",
 *   "errors": { "title": "must not be blank", "totalPrice": "must not be null" } }
 *
 * [2] BusinessException:
 * { "code": "G002", "message": "그룹이 가득 찼습니다" }
 *
 * ==================== 사용법 ====================
 * import { parseError } from '@/utils/errorParser'
 *
 * catch (error) {
 *   this.errorMessages = parseError(error, FIELD_LABELS, '처리에 실패했습니다.')
 * }
 */

// ==================== Spring Validation 메시지 → 한국어 매핑 ====================
// @NotBlank, @NotNull, @Positive, @Min, @Email 등의 기본 영문 메시지를 한국어로 변환
const VALIDATION_MESSAGES = {
  'must not be blank': '필수 입력 항목입니다',
  'must not be null': '필수 입력 항목입니다',
  'must be greater than 0': '0보다 큰 값을 입력해주세요',
  'must be a positive number': '양수를 입력해주세요',
  'must be greater than or equal to 2': '최소 2명 이상이어야 합니다',
  'must be a well-formed email address': '올바른 이메일 형식이 아닙니다',
}

/**
 * axios 에러 객체를 메시지 배열로 변환
 *
 * @param {Object} error - axios catch 블록의 error 객체
 * @param {Object} fieldLabels - 백엔드 필드명 → 한국어 라벨 매핑 객체 (각 페이지에서 정의)
 * @param {string} defaultMessage - 파싱 불가 시 표시할 기본 메시지
 * @returns {string[]} 화면에 표시할 에러 메시지 배열
 */
export function parseError(error, fieldLabels = {}, defaultMessage = '처리에 실패했습니다.') {
  const data = error.response?.data

  if (data?.errors) {
    // [케이스 1] Validation 에러: errors 맵을 필드별 한국어 메시지로 변환
    // Object.entries()로 { 필드명: 에러메시지 } 쌍을 배열로 변환
    return Object.entries(data.errors).map(([field, msg]) => {
      // 필드명을 한국어 라벨로 변환 (매핑 없으면 원래 필드명 그대로)
      const label = fieldLabels[field] || field
      // 영문 Validation 메시지를 한국어로 변환 (매핑 없으면 원래 메시지 그대로)
      const message = VALIDATION_MESSAGES[msg] || msg
      return `${label}: ${message}`
    })
  }

  if (data?.message) {
    // [케이스 2] BusinessException: 단일 한국어 메시지
    return [data.message]
  }

  // [케이스 3] 네트워크 오류 등 예상치 못한 에러
  return [defaultMessage]
}
