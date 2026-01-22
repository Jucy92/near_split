// Axios: HTTP 통신 라이브러리 (fetch API보다 편리함)
import axios from 'axios'

// ===========================
// Axios 인스턴스 생성 (기본 설정 적용)
// ===========================
const apiClient = axios.create({
  // baseURL: 모든 API 요청의 기본 주소
  // 예: apiClient.get('/auth/login') → http://localhost:8080/api/auth/login
  baseURL: 'http://localhost:8080/api',  // Spring Boot 백엔드 주소

  // timeout: 요청 시간 제한 (10초)
  // 10초 안에 응답 없으면 에러 발생
  timeout: 10000,

  // headers: 모든 요청에 자동으로 포함되는 헤더
  headers: {
    'Content-Type': 'application/json'  // JSON 형식으로 데이터 전송
  },

  // withCredentials: 쿠키를 자동으로 포함 (JWT 토큰이 쿠키로 저장됨)
  // true로 설정하면 모든 요청에 쿠키 자동 첨부
  withCredentials: true
})

// ===========================
// 요청 인터셉터: 모든 요청이 서버로 가기 "전"에 실행
// ===========================
// 용도: 요청 헤더 추가, 로깅, 토큰 추가 등
apiClient.interceptors.request.use(
  // 요청 성공 시 실행되는 함수
  (config) => {
    // config: 요청 설정 객체 (url, method, headers, data 등)

    // 예: Authorization 헤더에 JWT 토큰 추가 (선택사항)
    // 현재는 쿠키로 토큰을 보내므로 필요 없음
    // const token = document.cookie.match(/accessToken=([^;]+)/)?.[1]
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }

    console.log('API 요청:', config.method.toUpperCase(), config.url)

    return config  // 수정된 config를 반환 (요청 계속 진행)
  },
  // 요청 실패 시 실행되는 함수
  (error) => {
    console.error('요청 에러:', error)
    return Promise.reject(error)  // 에러를 그대로 전달
  }
)

// ===========================
// 응답 인터셉터: 서버 응답을 받은 "후"에 실행
// ===========================
// 용도: 에러 처리, 로깅, 자동 로그인 페이지 이동 등
apiClient.interceptors.response.use(
  // 응답 성공 시 (status 200-299) 실행되는 함수
  (response) => {
    // response: 서버 응답 객체
    // response.data: 실제 데이터
    // response.status: HTTP 상태 코드 (200, 201 등)
    // response.headers: 응답 헤더

    console.log('API 응답:', response.status, response.config.url)

    return response  // 응답을 그대로 반환
  },
  // 응답 실패 시 (status 400-599) 실행되는 함수
  (error) => {
    // error.response: 서버 응답 객체 (에러 정보 포함)
    // error.response.status: HTTP 상태 코드 (401, 403, 500 등)
    // error.response.data: 에러 메시지

    if (error.response) {
      const status = error.response.status

      // 401 Unauthorized: 인증 실패 (JWT 토큰 만료 or 없음)
      if (status === 401) {
        alert('로그인이 필요합니다.')
        window.location.href = '/login'  // 로그인 페이지로 강제 이동
      }
      // 403 Forbidden: 권한 없음
      else if (status === 403) {
        alert('권한이 없습니다.')
      }
      // 500 Internal Server Error: 서버 에러
      else if (status === 500) {
        alert('서버 오류가 발생했습니다.')
      }

      console.error('API 에러:', status, error.response.data)
    } else {
      // 네트워크 에러 (서버 응답 자체가 없음)
      alert('네트워크 오류가 발생했습니다.')
      console.error('네트워크 에러:', error.message)
    }

    return Promise.reject(error)  // 에러를 그대로 전달 (호출한 곳에서 catch로 잡을 수 있음)
  }
)

// apiClient를 export (다른 파일에서 사용)
export default apiClient
