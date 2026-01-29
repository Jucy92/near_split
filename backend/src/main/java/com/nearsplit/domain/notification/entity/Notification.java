package com.nearsplit.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
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
    private boolean isRead;                 // 읽음 여부
    @CreatedDate
    private LocalDateTime createdAt;        // 생성일
    @LastModifiedDate
    private LocalDateTime updatedAt;        // 수정일

    public void markAsRead() {
        this.isRead = true;
    }

}
