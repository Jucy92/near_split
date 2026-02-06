package com.nearsplit.external.toss.client;

import com.nearsplit.external.toss.dto.TossPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * packageName  : com.nearsplit.external.toss.client
 * fileName     : TossPaymentClient
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 토스페이먼츠 API 클라이언트
 *                  - 결제 승인, 조회, 취소 API 호출
 *                  - Basic Auth 인증 처리
 * 사용 API:
 *   POST /v1/payments/confirm           - 결제 승인
 *   GET  /v1/payments/{paymentKey}      - 결제 조회
 *   POST /v1/payments/{paymentKey}/cancel - 결제 취소
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

@Component
@Slf4j
public class TossPaymentClient {

    private final RestClient restClient;
    private static final String BASE_URL = "https://api.tosspayments.com/v1";


    public TossPaymentClient(@Value("${toss.payments.secret-key}") String secretKey) {
        // secretKey + ":" → Base64 인코딩 → "Basic " 접두어
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encoded;

        // RestClient 빌더로 기본 설정
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", authHeader)
                .defaultHeader("Content-Type", "application/json")
                .build();

        log.info("TossPaymentClient 초기화 완료: baseUrl={}", BASE_URL);
    }

    // 결제 승인 (API 호출 => POST https://api.tosspayments.com/v1/payments/confirm)
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Integer amount) {
        log.info("토스 결제 승인 요청: paymentKey={}, orderId={}, amount={}",
                paymentKey, orderId, amount);

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/payments/confirm")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, resp) -> {     // 첫 번째 인자 = 언제 실행할지, 두 번째 인자 = 무엇을 실행할지
                        log.error("토스 결제 승인 실패: status={}", resp.getStatusCode());
                        throw new RuntimeException("결제 승인에 실패했습니다.");
                    })
                    .body(TossPaymentResponse.class);

            log.info("토스 결제 승인 성공: status={}", response.getStatus());
            return response;

        } catch (Exception e) {
            log.error("토스 결제 승인 에러: {}", e.getMessage());
            throw new RuntimeException("결제 승인에 실패했습니다: " + e.getMessage());
        }
    }


    // 결제 조회 (API 호출 => GET https://api.tosspayments.com/v1/payments/{paymentKey})
    public TossPaymentResponse getPayment(String paymentKey) {
        log.info("토스 결제 조회 요청: paymentKey={}", paymentKey);

        try {
            return restClient.get()
                    .uri("/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .body(TossPaymentResponse.class);

        } catch (Exception e) {
            log.error("토스 결제 조회 에러: {}", e.getMessage());
            throw new RuntimeException("결제 조회에 실패했습니다: " + e.getMessage());
        }
    }


    // 결제 취소 (API 호출 => POST https://api.tosspayments.com/v1/payments/{paymentKey}/cancel)
    public TossPaymentResponse cancelPayment(String paymentKey, String cancelReason) {
        log.info("토스 결제 취소 요청: paymentKey={}, reason={}", paymentKey, cancelReason);

        Map<String, Object> body = Map.of("cancelReason", cancelReason);

        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/payments/{paymentKey}/cancel", paymentKey)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, resp) -> {
                        log.error("토스 결제 취소 실패: status={}", resp.getStatusCode());
                        throw new RuntimeException("결제 취소에 실패했습니다.");
                    })
                    .body(TossPaymentResponse.class);

            log.info("토스 결제 취소 성공: status={}", response.getStatus());
            return response;

        } catch (Exception e) {
            log.error("토스 결제 취소 에러: {}", e.getMessage());
            throw new RuntimeException("결제 취소에 실패했습니다: " + e.getMessage());
        }
    }
}
