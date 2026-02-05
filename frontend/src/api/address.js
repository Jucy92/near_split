/**
 * 파일: address.js
 * 설명: 주소 검색 API 호출 함수
 *       - 백엔드를 통해 juso.go.kr API 호출 (CORS 우회)
 *       - 프론트엔드에서 직접 juso.go.kr 호출 불가 (CORS 차단)
 *
 * API 엔드포인트:
 * | 기능       | 메서드 | 엔드포인트               | 설명                  |
 * |------------|--------|--------------------------|----------------------|
 * | 주소 검색  | GET    | /api/address/search      | 도로명주소 검색       |
 */
import apiClient from './axios'

/**
 * 주소 검색 API
 *
 * @param {string} keyword - 검색 키워드 (예: "역삼동", "테헤란로 123")
 * @returns {Promise} - 주소 목록 배열
 *
 * 응답 예시:
 * [
 *   {
 *     "roadAddr": "서울특별시 강남구 역삼로 123",
 *     "jibunAddr": "서울특별시 강남구 역삼동 123-45",
 *     "zipNo": "06241",
 *     "bdNm": "역삼타워",
 *     "siNm": "서울특별시",
 *     "sggNm": "강남구",
 *     "emdNm": "역삼동"
 *   }
 * ]
 */
export const searchAddress = (keyword) => {
  // GET /api/address/search?keyword=역삼동
  return apiClient.get('/address/search', {
    params: { keyword }
  })
}
