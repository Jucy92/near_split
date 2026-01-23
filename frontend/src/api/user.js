// User API 함수 모음
import apiClient from './axios'

// 내 프로필 조회
// GET /api/users/me
export const getMyProfile = () => {
  return apiClient.get('/users/me')
}

// 내 프로필 수정
// PATCH /api/users/me
export const updateMyProfile = (data) => {
  return apiClient.patch('/users/me', data)
}
