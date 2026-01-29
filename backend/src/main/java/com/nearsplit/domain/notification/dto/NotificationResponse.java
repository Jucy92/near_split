package com.nearsplit.domain.notification.dto;

import com.nearsplit.domain.notification.entity.Notification;
import com.nearsplit.domain.notification.entity.NotificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * packageName  : com.nearsplit.domain.notification.dto
 * fileName     : NotificationResponse
 * author       : user
 * date         : 2026-01-28(수)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-28(수)                user            최초 생성
 */
@Getter @Builder
public class NotificationResponse {
    private Long id;                        // 알림 번호
    private Long userId;                    // 알림 받을 사용자
    private NotificationType type;          // 알림 종류
    private String title;                   // 알림 제목
    private String message;                 // 알림 내용
    private Long referenceId;               // 관련 그룹 ID
    private boolean isRead;                 // 읽음 여부
    private LocalDateTime createdAt;        // 생성 일자

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
