import apiClient from './axios'

// ===========================
// 회원가입 API
// ===========================
// POST /api/auth/register
// 새 사용자 등록 요청
export const register = (data) => {
  return apiClient.post('/auth/register', data)
}

// ===========================
// 로그인 API
// ===========================
// POST /api/auth/login
// 이메일/비밀번호로 로그인
// 성공 시 서버에서 accessToken, refreshToken 쿠키 설정
export const login = (data) => {
  return apiClient.post('/auth/login', data)
}

// ===========================
// 로그아웃 API
// ===========================
// POST /api/auth/logout
// 서버에서 쿠키 삭제 처리
export const logout = () => {
  return apiClient.post('/auth/logout')
}

// ===========================
// 토큰 갱신 API
// ===========================
// POST /api/auth/refresh
// accessToken 만료 시 refreshToken으로 새 accessToken 발급
// 쿠키에 있는 refreshToken을 서버가 읽어서 검증
// 성공 시 새 accessToken이 쿠키에 설정됨
export const refreshToken = () => {
  return apiClient.post('/auth/refresh')
}
