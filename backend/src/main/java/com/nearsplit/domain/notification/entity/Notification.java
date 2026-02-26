package com.nearsplit.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * packageName  : com.nearsplit.domain.notification.entity
 * fileName     : Notification
 * author       : user
 * date         : 2026-01-27(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-27(화)                user            최초 생성
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;                    // 알림 받을 사용자
    @Enumerated(EnumType.STRING)
    private NotificationType type;          // 알림 종류
    private String title;                   // 알림 제목
    private String message;                 // 알림 내용
    private Long referenceId;               // 관련 그룹 ID
    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;    // 관련 그룹 테이블명
    @Builder.Default
    private boolean isRead = false;         // 읽음 여부
    @CreatedDate
    private LocalDateTime createdAt;        // 생성일
    @LastModifiedDate
    private LocalDateTime updatedAt;        // 수정일

    // ========================================
    // 정적 팩토리 메서드
    // ========================================

    /**
     * 알림 생성
     * - 읽음 여부 false로 초기화
     */
    public static Notification createNotification(Long userId, NotificationType type,
                                                   String title, String message,
                                                   Long referenceId, ReferenceType referenceType) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();
    }

    // ========================================
    // 도메인 메서드
    // ========================================

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 본인 알림인지 검증
     */
    public void validateOwner(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new IllegalArgumentException("사용자 정보가 다릅니다.");
        }
    }
}
