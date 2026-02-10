package com.nearsplit.domain.payment.service;

import com.nearsplit.domain.payment.dto.PaymentConfirmRequest;
import com.nearsplit.domain.payment.dto.PaymentResponse;
import com.nearsplit.domain.payment.entity.Payment;
import com.nearsplit.domain.payment.repository.PaymentRepository;
import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import com.nearsplit.external.toss.client.TossPaymentClient;
import com.nearsplit.external.toss.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * packageName  : com.nearsplit.domain.payment.service
 * fileName     : PaymentService
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 결제 비즈니스 로직
 *                  - TossPaymentClient를 통해 토스페이먼츠 API 연동
 *                  - 결제 정보 DB 저장 및 조회
 * 결제 흐름:
 *   1. 프론트엔드에서 결제 완료 후 successUrl로 리다이렉트
 *   2. 프론트엔드가 paymentKey, orderId, amount를 백엔드로 전송
 *   3. TossPaymentClient로 토스 API 결제 승인 요청
 *   4. 승인 성공 시 DB에 결제 정보 저장
 *   5. 프론트엔드에 결과 반환
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    // Repository 의존성
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SplitGroupRepository splitGroupRepository;
    private final ParticipantRepository participantRepository;

    // 토스페이먼츠 API 클라이언트
    private final TossPaymentClient tossPaymentClient;

    // ========================================
    // 결제 승인 API
    // ========================================

    /**
     * 결제 승인 처리
     *
     * 흐름:
     *   1. 중복 결제 확인
     *   2. TossPaymentClient로 토스 API 결제 승인 호출
     *   3. 성공 시 Payment 엔티티 생성 → DB 저장
     *   4. PaymentResponse 반환
     */
    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request, Long userId) {
        log.info("결제 승인 요청: paymentKey={}, orderId={}, amount={}",
                request.getPaymentKey(), request.getOrderId(), request.getAmount());

        // 1. 중복 결제 확인
        if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
            throw new RuntimeException("이미 처리된 결제입니다.");
        }

        // 2. TossPaymentClient로 결제 승인 API 호출
        TossPaymentResponse tossResponse = tossPaymentClient.confirmPayment(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );

        // 3. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 4. 그룹 조회 및 참여자 조회 (값 변경 처리)
        SplitGroup group = splitGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("그룹 정보를 찾을 수 없습니다."));

        Participant participant = participantRepository.findBySplitGroupIdAndUserId(group.getId(), userId)
                .orElseThrow(() -> new RuntimeException("일치하는 참여자 정보가 없습니다"));

        if (participant.getStatus() != ParticipantStatus.APPROVED) {    // 프론트를 믿지 말자...
            throw  new RuntimeException("참여자가 승인 상태가 아닙니다");
        }
        participant.setStatus(ParticipantStatus.PAID);



        // 5. Payment 엔티티 생성 및 저장
        Payment payment = Payment.createFromTossResponse(tossResponse, user, participant.getSplitGroup());
        paymentRepository.save(payment);

        log.info("결제 승인 성공: paymentKey={}, status={}",
                payment.getPaymentKey(), payment.getStatus());

        // 6. 응답 반환
        return PaymentResponse.from(payment);
    }

    // paymentKey로 결제 정보 조회 (DB에서)
    public PaymentResponse getPayment(String paymentKey) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
        return PaymentResponse.from(payment);
    }

    // 내 결제 내역 조회
    public List<PaymentResponse> getMyPayments(Long userId) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }


    // 결제 취소
    @Transactional
    public PaymentResponse cancelPayment(String paymentKey, String cancelReason, Long userId) {
        log.info("결제 취소 요청: paymentKey={}, reason={}", paymentKey, cancelReason);

        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 2. 권한 확인 (본인만 취소 가능)
        if (!payment.getUser().getId().equals(userId)) {
            throw new RuntimeException("결제 취소 권한이 없습니다.");
        }

        // 3. TossPaymentClient로 결제 취소 API 호출
        tossPaymentClient.cancelPayment(paymentKey, cancelReason);

        // 4. DB 상태 업데이트
        payment.cancel();   // payment 상태 값 변경 (DONE -> CANCELED)

        if (payment.getGroup() == null) {
            throw new RuntimeException("결제 정보의 그룹 정보가 비어있습니다. 확인 바랍니다.");
        }

        Participant participant = participantRepository.findBySplitGroupIdAndUserId(payment.getGroup().getId(), payment.getUser().getId())
                .orElseThrow(() -> new RuntimeException("참여자 정보를 찾을 수 없습니다."));

        if (participant.getStatus() != ParticipantStatus.PAID) {
            throw new RuntimeException("결제된 상태에서만 취소할 수 있습니다.");
        }
        participant.setStatus(ParticipantStatus.APPROVED);

        log.info("결제 취소 성공: paymentKey={}", paymentKey);

        return PaymentResponse.from(payment);
    }
}
