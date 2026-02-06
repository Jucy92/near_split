package com.nearsplit.domain.payment.dto;

import com.nearsplit.domain.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
/**
 * packageName  : com.nearsplit.domain.payment.dto
 * fileName     : PaymentResponse
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 결제 정보 응답 DTO
 *                  - 백엔드 → 프론트엔드로 반환하는 결제 정보
 *                  - Payment 엔티티를 API 응답용으로 변환
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

@Getter
@Builder
public class PaymentResponse {

    private Long id;                        // 결제 ID (DB PK)

    private String paymentKey;              // 토스페이먼츠 결제 키 (결제 조회, 취소 등에 사용)
    private String orderId;                 // 주문 ID
    private String orderName;               // 주문명
    private Integer amount;                 // 결제 금액


    private String method;                  // 결제 수단 ("카드", "계좌이체", "가상계좌")

    private String status;                  // 결제 상태 ("DONE", "CANCELED")
    private Long userId;                    // 결제한 사용자 ID
    private Long groupId;                   // 관련 그룹 ID
    private String cardCompany;             // 카드 정보 (카드 결제 시)
    private String cardNumber;
    private LocalDateTime approvedAt;       // 결제 승인 시각
    private LocalDateTime createdAt;        // 결제 생성 시각


    // Payment 엔티티를 PaymentResponse DTO로 변환
    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrderId())
                .orderName(payment.getOrderName())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus().name())
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .groupId(payment.getGroup() != null ? payment.getGroup().getId() : null)
                .cardCompany(payment.getCardCompany())
                .cardNumber(payment.getCardNumber())
                .approvedAt(payment.getApprovedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
