package com.nearsplit.domain.notification.service;

import com.nearsplit.common.exception.BusinessException;
import com.nearsplit.common.exception.ErrorCode;
import com.nearsplit.domain.notification.dto.NotificationResponse;
import com.nearsplit.domain.notification.entity.Notification;
import com.nearsplit.domain.notification.entity.NotificationType;
import com.nearsplit.domain.notification.entity.ReferenceType;
import com.nearsplit.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * packageName  : com.nearsplit.domain.notification.service
 * fileName     : NotificationService
 * author       : user
 * date         : 2026-01-27(화)
 * description   : 타 서비스에서 해당 서비스 호출하면서 넘겨준 값 처리..?
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-27(화)                user            최초 생성
 */

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createNotification(Long userId, NotificationType type, String title, String message,
                                   Long referenceId, ReferenceType referenceType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = NotificationResponse.from(saved);
    
        // 중복 추가되지 않게 체크로직도 넣어야함
        
        messagingTemplate.convertAndSend(
                "/topic/notification/" + userId, response);
    }

    @Transactional
    public void deletedNotification(Long userId, Long referenceId, ReferenceType referenceType, NotificationType type) {
        //int deletedRow = notificationRepository.deleteByUserIdAndReferenceIdAndReferenceType(userId, referenceId, referenceType);
        //Notification target = notificationRepository.findByUserIdAndReferenceIdAndReferenceTypeAndType(userId, referenceId, referenceType, type).orElse(null);
        Notification target = notificationRepository.findFirstByUserIdAndReferenceIdAndReferenceTypeAndTypeOrderByCreatedAtDesc
                                                        (userId, referenceId, referenceType, type).orElse(null);
        if (target == null) return;

        Map<String, Object> response = new HashMap<>();
        response.put("action", "DELETE");
        response.put("notificationId", target.getId());

        messagingTemplate.convertAndSend("/topic/notification/" + userId, response);
        log.info("target id={}", target.getId());
        notificationRepository.delete(target);

    }

    // 알림 목록 조회
    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 읽지 않은 알림 개수 조회
    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // 단일 알림 읽기 처리
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 정보가 다릅니다.");
        }
        notification.markAsRead();
        //notificationRepository.save(notification);
    }

    // 전체 알림 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notRead = notificationRepository.findByUserIdAndIsReadFalse(userId);

        for (Notification notification : notRead) {
            notification.markAsRead();
        }

    }

}
