package com.nearsplit.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일: TossPaymentResponse.java
 * 설명: 토스페이먼츠 결제 승인 API 응답 DTO
 *       - POST /v1/payments/confirm 응답을 매핑
 *       - 토스 API가 반환하는 JSON 구조와 1:1 대응
 *       - @JsonIgnoreProperties: 사용하지 않는 필드 무시 (API 변경에 유연)
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // 정의되지 않은 필드 무시
public class TossPaymentResponse {


    private String paymentKey;              // 결제 고유 키 (토스페이먼츠에서 발급)
    private String orderId;                 // 주문 ID (프론트엔드에서 생성)
    private String orderName;               // 주문명

    /**
     * - READY: 결제 생성됨
     * - IN_PROGRESS: 결제 진행 중
     * - WAITING_FOR_DEPOSIT: 입금 대기 (가상계좌)
     * - DONE: 결제 완료 ✓
     * - CANCELED: 전액 취소
     * - PARTIAL_CANCELED: 부분 취소
     * - ABORTED: 결제 승인 실패
     * - EXPIRED: 결제 만료
     */
    private String status;                  // 결제 상태

    /**
     * 결제 요청 시각 (ISO 8601)
     * 예: "2024-01-25T10:00:00+09:00"
     */
    private String requestedAt;

    /**
     * 결제 승인 시각 (ISO 8601)
     * - status가 DONE일 때만 값이 있음
     */
    private String approvedAt;

    /**
     * 결제 수단
     * - "카드", "가상계좌", "계좌이체", "휴대폰", "문화상품권" 등
     */
    private String method;

    /**
     * 총 결제 금액
     */
    private Integer totalAmount;

    /**
     * 취소 가능 금액
     * - 부분 취소 시 감소
     */
    private Integer balanceAmount;

    /**
     * 공급가액
     */
    private Integer suppliedAmount;

    /**
     * 부가세
     */
    private Integer vat;

    /**
     * 면세 금액
     */
    private Integer taxFreeAmount;

    /**
     * 통화
     * 예: "KRW"
     */
    private String currency;

    // ========================================
    // 결제 수단별 상세 정보 (해당 수단만 값 있음)
    // ========================================

    /**
     * 카드 결제 정보
     * - method가 "카드"일 때만 값이 있음
     */
    private CardInfo card;

    /**
     * 가상계좌 정보
     */
    private VirtualAccountInfo virtualAccount;

    /**
     * 계좌이체 정보
     */
    private TransferInfo transfer;

    /**
     * 간편결제 정보 (카카오페이, 네이버페이 등)
     */
    private EasyPayInfo easyPay;

    // ========================================
    // 기타 정보
    // ========================================

    /**
     * 영수증 정보
     */
    private ReceiptInfo receipt;

    /**
     * 실패 정보
     * - 결제 실패 시에만 값이 있음
     */
    private FailureInfo failure;

    /**
     * 에스크로 사용 여부
     */
    private Boolean useEscrow;

    /**
     * 부분 취소 가능 여부
     */
    private Boolean isPartialCancelable;

    /**
     * 결제 타입
     * - NORMAL: 일반 결제
     * - BILLING: 자동 결제
     * - BRANDPAY: 브랜드페이
     */
    private String type;

    // ========================================
    // 내부 클래스: 결제 수단별 상세 정보
    // ========================================

    /**
     * 카드 결제 상세 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardInfo {
        /**
         * 카드사 코드
         * 예: "4V" (삼성카드)
         */
        private String issuerCode;

        /**
         * 매입사 코드
         */
        private String acquirerCode;

        /**
         * 마스킹된 카드 번호
         * 예: "12345678****789*"
         */
        private String number;

        /**
         * 할부 개월 수
         * - 0: 일시불
         */
        private Integer installmentPlanMonths;

        /**
         * 무이자 할부 여부
         */
        private Boolean isInterestFree;

        /**
         * 승인 번호
         */
        private String approveNo;

        /**
         * 카드 포인트 사용 여부
         */
        private Boolean useCardPoint;

        /**
         * 카드 타입
         * - "신용", "체크", "기프트", "미확인"
         */
        private String cardType;

        /**
         * 소유자 타입
         * - "개인", "법인", "미확인"
         */
        private String ownerType;

        /**
         * 카드 결제 금액
         */
        private Integer amount;
    }

    /**
     * 가상계좌 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VirtualAccountInfo {
        private String accountType;     // 계좌 타입 (일반, 고정)
        private String accountNumber;   // 계좌번호
        private String bankCode;        // 은행 코드
        private String customerName;    // 예금주
        private String dueDate;         // 입금 기한
        private String refundStatus;    // 환불 상태
        private Boolean expired;        // 만료 여부
    }

    /**
     * 계좌이체 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransferInfo {
        private String bankCode;        // 은행 코드
        private String settlementStatus;// 정산 상태
    }

    /**
     * 간편결제 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EasyPayInfo {
        private String provider;        // 간편결제 제공자 (카카오페이, 네이버페이 등)
        private Integer amount;         // 결제 금액
        private Integer discountAmount; // 할인 금액
    }

    /**
     * 영수증 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReceiptInfo {
        /**
         * 영수증 URL
         */
        private String url;
    }

    /**
     * 실패 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailureInfo {
        /**
         * 에러 코드
         * 예: "REJECT_CARD_COMPANY"
         */
        private String code;

        /**
         * 에러 메시지
         * 예: "카드사에서 결제를 거절했습니다."
         */
        private String message;
    }
}
