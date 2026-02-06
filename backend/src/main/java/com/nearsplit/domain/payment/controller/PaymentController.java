package com.nearsplit.domain.payment.controller;

import com.nearsplit.domain.payment.dto.PaymentConfirmRequest;
import com.nearsplit.domain.payment.dto.PaymentResponse;
import com.nearsplit.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * packageName  : com.nearsplit.domain.payment.controller
 * fileName     : PaymentController
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 결제 API 컨트롤러
 *                  - 결제 승인, 조회, 취소 엔드포인트
 *  API 목록:
 *    POST /api/payments/confirm     - 결제 승인 (토스 → 우리 서버)
 *    GET  /api/payments/{paymentKey} - 결제 정보 조회
 *    GET  /api/payments/my          - 내 결제 내역 조회
 *    POST /api/payments/{paymentKey}/cancel - 결제 취소
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    /**
     * 결제 승인 요청
     *
     * 프론트엔드에서 토스페이먼츠 결제 완료 후
     * successUrl로 리다이렉트되면 이 API를 호출함
     *
     * 요청 예시:
     *   POST /api/payments/confirm
     *   Body: {
     *     "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw...",
     *     "orderId": "GROUP_5_1706123456789_abc123",
     *     "amount": 15000,
     *     "groupId": 5
     *   }
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        PaymentResponse response = paymentService.confirmPayment(request, userId);
        return ResponseEntity.ok(response);
    }


    // paymentKey로 결제 정보 조회
    @GetMapping("/{paymentKey}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentKey) {
        PaymentResponse response = paymentService.getPayment(paymentKey);
        return ResponseEntity.ok(response);
    }


    // 내 결제 내역 조회
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal Long userId
    ) {
        List<PaymentResponse> response = paymentService.getMyPayments(userId);
        return ResponseEntity.ok(response);
    }


    /**
     * 결제 취소
     * 요청 예시:
     *   POST /api/payments/{paymentKey}/cancel
     *   Body: { "cancelReason": "고객 요청" }
     */
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal Long userId
    ) {
        String cancelReason = requestBody.get("cancelReason");
        PaymentResponse response = paymentService.cancelPayment(paymentKey, cancelReason, userId);
        return ResponseEntity.ok(response);
    }
}
