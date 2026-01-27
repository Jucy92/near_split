package com.nearsplit.domain.split_group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder
@RequiredArgsConstructor
@Table(name = "split_group")
@AllArgsConstructor
@ToString
public class SplitGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@OneToOne(fetch = FetchType.LAZY) // 값을 사용하긴 하는데.. 조인은 아니다..?
    //@JoinColumn(name = "host_user_id", nullable = false)
    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;

    //@OneToMany
    @JoinColumn(name = "product_id")
    private Long productId; // 이게 필요할까... 우리가 품목코드를 관리하느것도 아닌데.. 아니면 그냥 String으로 해서 파스타, 고구마 이런식으로 받거나..
    @Column(length = 50)
    private String title;
    private BigDecimal totalPrice;
    private int maxParticipants;
    @Builder.Default
    private int currentParticipants = 0;
    private String pickupLocation;
    private String pickupLocationGeo;
    private LocalDate pickupDate;               // 이건 생성 때가 아니라.. 파티원 다 모아지고 결정해서 입력하는게 낫겠지..?
    private LocalDate closedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SplitGroupStatus status = SplitGroupStatus.RECRUITING;  // RECRUITING, FULL, CLOSED, COMPLETED, CANCELLED (모집중, 모집완료, 거래종료, 완료(?), 취소)
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 주의사항:
     *   - @OneToMany(mappedBy = "splitGroup"): participant의 splitGroup 필드와 매핑
     *   - cascade = CascadeType.ALL: SplitGroup 삭제 시 참여자도 모두 삭제
     *   - orphanRemoval = true: 참여자 목록에서 제거 시 DB에서도 삭제
     *   - @Builder.Default: participants를 빈 리스트로 초기화
     */
    //  ⭐ 중요: 참여자 목록 (1:N 관계)
    @OneToMany(mappedBy = "splitGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

}
