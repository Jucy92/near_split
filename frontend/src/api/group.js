// SplitGroup API 함수 모음
import apiClient from './axios'

// 전체 그룹 목록 조회 (페이징)
// GET /api/split?page=0&size=10
export const getGroups = (page = 0, size = 10) => {
  return apiClient.get('/split', { params: { page, size } })
}

// 내 그룹 목록 조회
// GET /api/split/my
export const getMyGroups = () => {
  return apiClient.get('/split/my')
}

// 그룹 상세 조회
// GET /api/split/{groupId}
export const getGroup = (groupId) => {
  return apiClient.get(`/split/${groupId}`)
}

// 그룹 생성
// POST /api/split
export const createGroup = (data) => {
  return apiClient.post('/split', data)
}

// 그룹 수정
// PATCH /api/split/{groupId}
export const updateGroup = (groupId, data) => {
  return apiClient.patch(`/split/${groupId}`, data)
}

// 그룹 삭제 (Soft Delete)
// DELETE /api/split/{groupId}
export const deleteGroup = (groupId) => {
  return apiClient.delete(`/split/${groupId}`)
}

// 그룹 참여 신청
// POST /api/split/{groupId}/join
export const joinGroup = (groupId) => {
  return apiClient.post(`/split/${groupId}/join`)
}

// 참여 취소
// DELETE /api/split/{groupId}/join
export const cancelJoin = (groupId) => {
  return apiClient.delete(`/split/${groupId}/join`)
}

// 참여자 승인
// POST /api/split/{groupId}/approve
// 백엔드 ParticipantActionRequest는 participantUserId 필드를 기대함
export const approveParticipant = (groupId, participantUserId) => {
  return apiClient.post(`/split/${groupId}/approve`, { participantUserId })
}

// 참여자 거절
// POST /api/split/{groupId}/reject
// 백엔드 ParticipantActionRequest는 participantUserId 필드를 기대함
export const rejectParticipant = (groupId, participantUserId) => {
  return apiClient.post(`/split/${groupId}/reject`, { participantUserId })
}

// 참여자 수 조회
// GET /api/split/{groupId}/participants
export const getParticipantCount = (groupId) => {
  return apiClient.get(`/split/${groupId}/participants`)
}
