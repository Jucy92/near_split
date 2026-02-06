package com.nearsplit.domain.payment.repository;

import com.nearsplit.domain.payment.entity.Payment;
import com.nearsplit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * packageName  : com.nearsplit.domain.payment.repository
 * fileName     : PaymentRepository
 * author       : user
 * date         : 2026-02-05(목)
 * description   : 결제 정보 데이터 접근 레이어
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-02-05(목)                user            최초 생성
 */

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // paymentKey로 결제 정보 조회
    Optional<Payment> findByPaymentKey(String paymentKey);

    // orderId로 결제 정보 조회
    Optional<Payment> findByOrderId(String orderId);

    // 사용자의 결제 내역 조회 (최신순 정렬)
    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    // 사용자 ID로 결제 내역 조회 (최신순 정렬)
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 그룹 ID로 결제 내역 조회
    List<Payment> findByGroupId(Long groupId);

    // 결제 키 존재 여부 확인 (중복 결제 방지용)
    boolean existsByPaymentKey(String paymentKey);

    // 주문 ID 존재 여부 확인(중복 주문 방지용)
    boolean existsByOrderId(String orderId);
}
