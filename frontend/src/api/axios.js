// Axios: HTTP 통신 라이브러리 (fetch API보다 편리함)
import axios from 'axios'

// ===========================
// Axios 인스턴스 생성 (기본 설정 적용)
// ===========================
const apiClient = axios.create({
  // baseURL: 모든 API 요청의 기본 주소
  // 예: apiClient.get('/auth/login') → http://localhost:8080/api/auth/login
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',  // Spring Boot 백엔드 주소

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
// 용도: 에러 처리, 로깅, 토큰 자동 갱신 등
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
  // async 함수로 변경: refresh 요청 시 await 사용하기 위함
  async (error) => {
    // error.response: 서버 응답 객체 (에러 정보 포함)
    // error.response.status: HTTP 상태 코드 (401, 403, 500 등)
    // error.response.data: 에러 메시지

    // 서버 응답이 있는 경우에만 처리
    if (error.response) {
      const status = error.response.status
      // 서버에서 보낸 에러 코드 추출 (예: AUTH-001, AUTH-002 등)
      const errorCode = error.response.data?.code

      // ===========================
      // 401 Unauthorized 처리
      // ===========================
      if (status === 401) {
        console.log('401 에러 발생, 에러코드:', errorCode)

        // ─────────────────────────────────────────
        // AUTH-002: Access Token 만료 → 갱신 시도
        // ─────────────────────────────────────────
        if (errorCode === 'AUTH-002') {
          console.log('Access Token 만료됨, Refresh 시도...')

          // 원래 요청 정보 저장 (재시도할 때 사용)
          // error.config: 실패한 요청의 설정 정보 (url, method, data 등)
          const originalRequest = error.config

          // ⭐ 무한 루프 방지: 이미 재시도한 요청인지 확인
          // _retry 플래그가 true면 이미 한번 갱신 시도한 것
          if (originalRequest._retry) {
            console.log('이미 재시도한 요청, 로그인 페이지로 이동')
            // 로그인 상태 초기화
            localStorage.removeItem('isLoggedIn')
            window.location.href = '/login'
            return Promise.reject(error)
          }

          // 재시도 플래그 설정 (다음번에 또 실패하면 무한루프 방지)
          originalRequest._retry = true

          try {
            // ─────────────────────────────────────────
            // Refresh Token으로 새 Access Token 발급 요청
            // ─────────────────────────────────────────
            // POST /api/auth/refresh 호출
            // 쿠키에 있는 refreshToken을 서버가 자동으로 읽음 (withCredentials: true)
            await apiClient.post('/auth/refresh')

            console.log('토큰 갱신 성공! 원래 요청 재시도...')

            // ─────────────────────────────────────────
            // 갱신 성공 → 원래 요청 재시도
            // ─────────────────────────────────────────
            // apiClient(originalRequest): 아까 실패한 요청을 다시 보냄
            // 이번엔 새로운 accessToken 쿠키가 포함되어 있음
            return apiClient(originalRequest)

          } catch (refreshError) {
            // ─────────────────────────────────────────
            // 갱신 실패 → 로그인 페이지로 이동
            // ─────────────────────────────────────────
            // refreshToken도 만료됐거나 유효하지 않음
            console.log('토큰 갱신 실패:', refreshError.response?.data?.code)

            // 로그인 상태 초기화 (localStorage 클리어)
            localStorage.removeItem('isLoggedIn')

            // 사용자에게 알림
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.')

            // 로그인 페이지로 강제 이동
            window.location.href = '/login'

            return Promise.reject(refreshError)
          }
        }

        // ─────────────────────────────────────────
        // AUTH-001: 인증 정보 없음 (토큰 자체가 없음)
        // AUTH-004: Refresh Token 만료
        // 기타 401 에러: 로그인 필요
        // ─────────────────────────────────────────
        console.log('인증 필요, 로그인 페이지로 이동')

        // 로그인 상태 초기화
        localStorage.removeItem('isLoggedIn')

        alert('로그인이 필요합니다.')
        window.location.href = '/login'
      }
      // ===========================
      // 403 Forbidden: 권한 없음
      // ===========================
      else if (status === 403) {
        alert('권한이 없습니다.')
      }
      // ===========================
      // 500 Internal Server Error: 서버 에러
      // ===========================
      else if (status === 500) {
        alert('서버 오류가 발생했습니다.')
      }

      console.error('API 에러:', status, error.response.data)
    } else {
      // ===========================
      // 네트워크 에러 (서버 응답 자체가 없음)
      // ===========================
      alert('네트워크 오류가 발생했습니다.')
      console.error('네트워크 에러:', error.message)
    }

    // 에러를 그대로 전달 (호출한 곳에서 catch로 잡을 수 있음)
    return Promise.reject(error)
  }
)

// apiClient를 export (다른 파일에서 사용)
export default apiClient
