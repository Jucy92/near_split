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
@RequiredArgsConstructor
@Getter @Setter @Builder
@AllArgsConstructor
@ToString
public class Participant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_group_id", nullable = false)
    private SplitGroup splitGroup;

    @Column(nullable = false)
    private Long userId;    // 참여자들 이름 보여줄려면 나중에 여기 User로 해서 가져와야할거같은데..
    private Integer quantity;
    private BigDecimal shareAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;  // PENDING, APPROVED, REJECTED, PAID, COMPLETED / 대기, 승인, 거절, 결제, 완료
    @CreatedDate
    private LocalDateTime joinedAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
