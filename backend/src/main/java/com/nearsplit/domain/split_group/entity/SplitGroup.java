package com.nearsplit.domain.split_group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "split_group")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class SplitGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;

    private Double latitude;
    private Double longitude;

    @JoinColumn(name = "product_id")
    private Long productId;
    @Column(length = 50)
    private String title;
    private BigDecimal totalPrice;
    private int maxParticipants;
    @Builder.Default
    private int currentParticipants = 0;
    private String pickupLocation;
    private String pickupLocationGeo;
    private LocalDate pickupDate;
    private LocalDate closedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SplitGroupStatus status = SplitGroupStatus.RECRUITING;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "splitGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    // ========================================
    // 정적 팩토리 메서드
    // ========================================

    /**
     * 소분 그룹 생성
     * - 마감일 검증 포함 (내일 이후만 허용)
     * - 상태: RECRUITING, 현재 참여자: 0으로 초기화
     */
    public static SplitGroup createGroup(Long hostUserId, String title, BigDecimal totalPrice,
                                         int maxParticipants, String pickupLocation, LocalDate closedAt) {
        validateClosedDate(closedAt);

        return SplitGroup.builder()
                .hostUserId(hostUserId)
                .title(title)
                .totalPrice(totalPrice)
                .maxParticipants(maxParticipants)
                .pickupLocation(pickupLocation)
                .closedAt(closedAt)
                .status(SplitGroupStatus.RECRUITING)
                .currentParticipants(0)
                .build();
    }

    // ========================================
    // 도메인 메서드 - 그룹 수정
    // ========================================

    /**
     * 그룹 정보 수정
     * - 참여자가 있으면 제목, 금액, 인원 변경 불가
     * - 마감일은 항상 내일 이후여야 함
     */
    public void updateGroup(String title, BigDecimal totalPrice, Integer maxParticipants,
                            String pickupLocation, String pickupLocationGeo, LocalDate closedAt) {
        boolean hasParticipants = hasParticipants();

        if (title != null) {
            if (hasParticipants) {
                throw new IllegalArgumentException("참여자가 있는 경우 제목 변경이 불가합니다.");
            }
            this.title = title;
        }
        if (totalPrice != null) {
            if (hasParticipants) {
                throw new IllegalArgumentException("참여자가 있는 경우 총 금액이 불가합니다.");
            }
            this.totalPrice = totalPrice;
        }
        if (maxParticipants != null) {
            if (hasParticipants) {
                throw new IllegalArgumentException("참여자가 있는 경우 총 인원 변경이 불가합니다.");
            }
            if (maxParticipants < 1) {
                throw new IllegalArgumentException("모집 인원이 1명 이상이어야 합니다.");
            }
            this.maxParticipants = maxParticipants;
        }
        if (pickupLocation != null) {
            this.pickupLocation = pickupLocation;
        }
        if (pickupLocationGeo != null) {
            this.pickupLocationGeo = pickupLocationGeo;
        }
        if (closedAt != null) {
            validateClosedDate(closedAt);
            this.closedAt = closedAt;
        }
    }

    // ========================================
    // 도메인 메서드 - 상태 전이
    // ========================================

    /**
     * 그룹 취소 (Soft Delete)
     * - RECRUITING 상태에서만 가능
     * - 참여자가 있으면 취소 불가
     */
    public void cancel() {
        if (this.status != SplitGroupStatus.RECRUITING) {
            throw new IllegalArgumentException("모집 중인 상태에서만 삭제가 가능합니다.");
        }
        if (hasParticipants()) {
            throw new IllegalArgumentException("참여자가 있는 경우 삭제가 불가 합니다.");
        }
        this.status = SplitGroupStatus.CANCELLED;
    }

    // ========================================
    // 도메인 메서드 - 참여 관련
    // ========================================

    /**
     * 참여 가능 여부 검증
     * - RECRUITING 상태이고 정원이 차지 않았을 때만 참여 가능
     */
    public void validateCanJoin() {
        if (this.status != SplitGroupStatus.RECRUITING) {
            throw new IllegalArgumentException("모집이 마감된 그룹입니다.");
        }
        if (isFull()) {
            throw new IllegalArgumentException("정원이 마감되었습니다.");
        }
    }

    /**
     * 방장 여부 검증
     */
    public void validateHost(Long userId) {
        if (!this.hostUserId.equals(userId)) {
            throw new IllegalArgumentException("방장만 수행할 수 있습니다.");
        }
    }

    /**
     * 참여자 승인
     * - 분담금 계산 + 인원 증가 + 정원 도달 시 FULL 전환
     * @return true: 정원이 다 차서 FULL이 됨 (알림 발송 트리거용)
     */
    public boolean approveParticipant(Participant participant) {
        participant.approve(calculateShareAmount());
        this.currentParticipants++;

        if (isFull()) {
            this.status = SplitGroupStatus.FULL;
            return true;    // 서비스에서 이 값을 보고 "모집 완료" 알림을 보냄
        }
        return false;
    }

    // ========================================
    // 도메인 메서드 - 계산
    // ========================================

    /**
     * 1인당 분담금 계산
     * - 총 금액 / (최대 인원 + 1) : +1은 방장 포함
     * - 소수점 2자리, 반내림
     */
    public BigDecimal calculateShareAmount() {
        return this.totalPrice.divide(
                BigDecimal.valueOf(this.maxParticipants + 1),
                2,
                RoundingMode.HALF_DOWN
        );
    }

    // ========================================
    // 상태 판단 메서드
    // ========================================

    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }

    public boolean hasParticipants() {
        return this.currentParticipants > 0;
    }

    public boolean isRecruiting() {
        return this.status == SplitGroupStatus.RECRUITING;
    }

    // ========================================
    // 내부 검증 메서드
    // ========================================

    private static void validateClosedDate(LocalDate closedAt) {
        if (closedAt != null && !closedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("마감일이 내일 이후로 설정해야 합니다.");
        }
    }
}
