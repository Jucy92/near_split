package com.nearsplit.domain.split_group.entity;

/**
 * packageName  : com.nearsplit.domain.split_group.dto
 * fileName     : ParticipantStatus
 * author       : user
 * date         : 2026-01-07(수)
 * description   : 그룹 참여 상태 관리(오타 예방)
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-07(수)          jcy            최초 생성
 */

public enum ParticipantStatus {
 PENDING,   // 대기
 APPROVED,  // 승인
 REJECTED,  // 거절
 PAID,      // 결제
 REFUNDED,  // 환불
 COMPLETED  // 완료
}
