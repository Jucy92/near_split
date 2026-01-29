package com.nearsplit.domain.notification.entity;

import lombok.Getter;

/**
 * packageName  : com.nearsplit.domain.notification.entity
 * fileName     : NotificationType
 * author       : user
 * date         : 2026-01-27(화)
 * description   : 알림 종류
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-27(화)                user            최초 생성
 */

@Getter
public enum NotificationType {
    JOIN_REQUEST,               // 참여 신청 (호스트에게)
    APPROVED,                   // 승인 (신청자에게)
    REJECTED,                   // 거절 (신청자에게)
    GROUP_FULL,                 // 모집 완료 (참여자 전원에게)
    CHAT                        // 새 채팅
}
