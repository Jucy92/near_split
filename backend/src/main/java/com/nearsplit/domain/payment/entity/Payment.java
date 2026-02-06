package com.nearsplit.domain.payment.entity;

import com.nearsplit.domain.payment.dto.PaymentStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.external.toss.dto.TossPaymentResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * packageName  : com.nearsplit.domain.payment.entity
 * fileName     : Payment
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 결제 정보 엔티티
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // 토스페이먼츠 관련 필드

    @Column(nullable = false, unique = true)
    private String paymentKey;                  // 토스페이먼츠에서 발급한 결제 키 (고유값) - 결제 조회, 취소 등에 사용하는 핵심 키

    @Column(nullable = false, unique = true)
    private String orderId;                     // 주문 ID (프론트 생성) - 형식: GROUP_{groupId}_{timestamp}_{random}

    @Column(nullable = false)
    private Integer amount;                     // 결제 금액

    private String orderName;                   // 주문명
    private String method;                      // 결제수단 (카드, 계좌이체 등)

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;               // 결제 상태 (READY, IN_PROGRESS, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)


    // 연관관계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                          // 결제한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private SplitGroup group;                   // 관련 그룹

    // 카드 정보 (카드 결제 시)

    private String cardCompany;                 // 카드사 코드
    private String cardNumber;                  // 마스킹된 카드 번호

    // 시간 정보

    private LocalDateTime approvedAt;           // 결제 승인 일시
    @CreationTimestamp
    private LocalDateTime createdAt;            // 결제 생성 일시 (DB 저장 시각)



    @Builder
    public Payment(String paymentKey, String orderId, Integer amount, String orderName,
                   String method, PaymentStatus status, User user, SplitGroup group,
                   String cardCompany, String cardNumber, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.orderName = orderName;
        this.method = method;
        this.status = status;
        this.user = user;
        this.group = group;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.approvedAt = approvedAt;
    }



    //  토스페이먼츠 응답으로 Payment 엔티티 생성
    public static Payment createFromTossResponse(TossPaymentResponse tossResponse,
                                                  User user,
                                                  SplitGroup group) {
        PaymentBuilder builder = Payment.builder()
                .paymentKey(tossResponse.getPaymentKey())
                .orderId(tossResponse.getOrderId())
                .orderName(tossResponse.getOrderName())
                .amount(tossResponse.getTotalAmount())
                .method(tossResponse.getMethod())
                .status(PaymentStatus.valueOf(tossResponse.getStatus()))
                .user(user)
                .group(group);

        // 승인 시각 변환 (ISO 8601 → LocalDateTime)
        if (tossResponse.getApprovedAt() != null) {
            LocalDateTime approvedAt = OffsetDateTime
                    .parse(tossResponse.getApprovedAt())
                    .toLocalDateTime();
            builder.approvedAt(approvedAt);
        }

        // 카드 정보 추출 (카드 결제인 경우)
        if (tossResponse.getCard() != null) {
            builder.cardCompany(tossResponse.getCard().getIssuerCode());
            builder.cardNumber(tossResponse.getCard().getNumber());
        }

        return builder.build();
    }

    // 상태 변경 메서드 (DDD)

    //  결제 취소 처리
    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }

    // 부분 취소 처리
    public void partialCancel(Integer canceledAmount) {
        this.status = PaymentStatus.PARTIAL_CANCELED;
        this.amount = this.amount - canceledAmount;
    }
}
