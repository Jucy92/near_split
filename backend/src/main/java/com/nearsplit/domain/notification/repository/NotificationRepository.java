package com.nearsplit.domain.notification.repository;

import com.nearsplit.domain.notification.entity.Notification;
import com.nearsplit.domain.notification.entity.NotificationType;
import com.nearsplit.domain.notification.entity.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * packageName  : com.nearsplit.domain.notification.repository
 * fileName     : NotificationRepository
 * author       : user
 * date         : 2026-01-27(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-27(화)                user            최초 생성
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);   // 내 알림 목록
    List<Notification> findByUserIdAndIsReadFalse(Long userId);         // 내 알림 중 알림을 읽지 않은 목록
    int countByUserIdAndIsReadFalse(Long userId);                       // 안 읽은 개수


    int deleteByUserIdAndReferenceIdAndReferenceType(Long userId, Long referenceId, ReferenceType referenceType);   // 이걸로 바로 삭제하려고 했는데, 프론트에 삭제되는 번호를 넘겨줘야해서 find로 추가

    Optional<Notification> findByUserIdAndReferenceIdAndReferenceTypeAndType(Long userId, Long referenceId, ReferenceType referenceType, NotificationType type);    // 같은 정보를 가진 데이터가 여러개 나옴 => 삭제시 선택 못함 => 아래 메서드 사용 
    Optional<Notification> findFirstByUserIdAndReferenceIdAndReferenceTypeAndTypeOrderByCreatedAtDesc(Long userId, Long referenceId, ReferenceType referenceType, NotificationType type);   // 내림차순으로 된 첫번째(findFirst) 로우만 호출

}
