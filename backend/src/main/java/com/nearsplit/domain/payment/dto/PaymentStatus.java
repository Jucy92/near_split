package com.nearsplit.domain.payment.dto;

/**
 * packageName  : com.nearsplit.domain.payment.dto
 * fileName     : PaymentStatus
 * author       : user
 * date         : 2026-02-05(목)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

public enum PaymentStatus {
    READY,           // 결제 대기
    IN_PROGRESS,     // 결제 진행 중
    DONE,            // 결제 완료
    CANCELED,        // 결제 취소
    PARTIAL_CANCELED,// 부분 취소
    ABORTED,         // 결제 승인 실패
    EXPIRED          // 결제 만료
}
