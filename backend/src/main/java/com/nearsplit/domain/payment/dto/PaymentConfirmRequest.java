package com.nearsplit.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * packageName  : com.nearsplit.domain.payment.dto
 * fileName     : PaymentConfirmRequest
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 결제 정보 요청 DTO
 *                  - 프론트엔드 → 백엔드로 요청하는 결제 정보
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {

    private String paymentKey;          // 토스페이먼츠에서 발급한 결제 키
    private String orderId;             // 주문 ID (프론트엔드에서 생성한 고유 주문 번호 - GROUP_{groupId}_{timestamp}_{random})
    private Integer amount;             // 결제 금액

    private Long groupId;               // 그룹 ID => 나중에 결제창 다른 곳에서도 사용하면.. 결제 시도한 도메인 정보도 따로 받아야할듯..
}
