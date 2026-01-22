import apiClient from './axios'

// 회원가입 API
export const register = (data) => {
  return apiClient.post('/auth/register', data)
}

// 로그인 API
export const login = (data) => {
  return apiClient.post('/auth/login', data)
}

// 로그아웃 API
export const logout = () => {
  return apiClient.post('/auth/logout')
}
