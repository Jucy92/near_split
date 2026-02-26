package com.nearsplit.domain.split_group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Participant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_group_id", nullable = false)
    private SplitGroup splitGroup;

    @Column(nullable = false)
    private Long userId;
    private Integer quantity;
    private BigDecimal shareAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;
    @CreatedDate
    private LocalDateTime joinedAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Transient
    private String nickname;

    // ========================================
    // 상태 전이 메서드
    // ========================================

    /**
     * 참여 승인
     * - 이미 승인된 참여자는 다시 승인 불가
     * - 분담금 설정
     */
    public void approve(BigDecimal shareAmount) {
        if (this.status == ParticipantStatus.APPROVED) {
            throw new IllegalArgumentException("이미 승인된 참여자입니다.");
        }
        this.status = ParticipantStatus.APPROVED;
        this.shareAmount = shareAmount;
    }

    /**
     * 참여 취소 가능 여부 검증
     * - PENDING 상태에서만 취소 가능
     */
    public void validateCancellable() {
        if (this.status != ParticipantStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 신청만 취소할 수 있습니다.");
        }
    }

    /**
     * 결제 완료 처리
     * - APPROVED 상태에서만 결제 가능
     */
    public void markAsPaid() {
        if (this.status != ParticipantStatus.APPROVED) {
            throw new IllegalArgumentException("승인된 참여자만 결제할 수 있습니다.");
        }
        this.status = ParticipantStatus.PAID;
    }

    /**
     * 결제 취소 처리
     * - PAID 상태에서만 결제 취소 가능
     * - 취소 시 APPROVED 상태로 복원
     */
    public void cancelPayment() {
        if (this.status != ParticipantStatus.PAID) {
            throw new IllegalArgumentException("결제된 상태에서만 취소할 수 있습니다.");
        }
        this.status = ParticipantStatus.APPROVED;
    }

    // ========================================
    // 편의 메서드
    // ========================================

    /**
     * 닉네임 설정 (조회 시 @Transient 필드에 세팅용)
     */
    public void assignNickname(String nickname) {
        this.nickname = nickname;
    }
}
