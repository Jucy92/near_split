package com.nearsplit.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;  // 어느 그룹의 채팅인지

    @Column(nullable = false)
    private Long senderId;  // 발신자 ID

    @Column(nullable = false, length = 50)
    private String senderName;  // 발신자 이름 (조회 편의성)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;  // 메시지 타입

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 메시지 타입
    public enum MessageType {
        CHAT,      // 일반 채팅
        JOIN,      // 입장 알림
        LEAVE,     // 퇴장 알림
        NOTICE     // 공지
    }
}
