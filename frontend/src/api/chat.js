// Chat API 함수 모음
import apiClient from './axios'

// 채팅 히스토리 조회 (페이징)
// GET /api/chat/{groupId}/history?page=0&size=50
export const getChatHistory = (groupId, page = 0, size = 50) => {
  return apiClient.get(`/chat/${groupId}/history`, { params: { page, size } })
}

// 최근 메시지 조회 (최신 20개)
// GET /api/chat/{groupId}/recent
export const getRecentMessages = (groupId) => {
  return apiClient.get(`/chat/${groupId}/recent`)
}
