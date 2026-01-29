/**
 * 파일: notification.js
 * 설명: 알림 관련 API 함수 모음
 *       - 알림 목록 조회
 *       - 읽지 않은 알림 개수 조회
 *       - 알림 읽음 처리 (단일/전체)
 */

import apiClient from './axios'

// ===========================
// 알림 목록 조회 API
// ===========================
// GET /api/notifications
// 현재 로그인한 사용자의 알림 목록 반환 (최신순 정렬)
export const getNotifications = () => {
  return apiClient.get('/notifications')
}

// ===========================
// 읽지 않은 알림 개수 조회 API
// ===========================
// GET /api/notifications/unread-count
// 읽지 않은(isRead=false) 알림 개수 반환
export const getUnreadCount = () => {
  return apiClient.get('/notifications/unread-count')
}

// ===========================
// 단일 알림 읽음 처리 API
// ===========================
// PATCH /api/notifications/{notificationId}/read
// 특정 알림을 읽음 상태로 변경
export const markAsRead = (notificationId) => {
  return apiClient.patch(`/notifications/${notificationId}/read`)
}

// ===========================
// 전체 알림 읽음 처리 API
// ===========================
// PATCH /api/notifications/read-all
// 현재 사용자의 모든 알림을 읽음 상태로 변경
export const markAllAsRead = () => {
  return apiClient.patch('/notifications/read-all')
}
