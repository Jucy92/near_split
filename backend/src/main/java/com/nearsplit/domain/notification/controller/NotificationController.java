package com.nearsplit.domain.notification.controller;

import com.nearsplit.common.dto.ApiResponse;
import com.nearsplit.domain.notification.dto.NotificationResponse;
import com.nearsplit.domain.notification.entity.Notification;
import com.nearsplit.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName  : com.nearsplit.domain.notification.controller
 * fileName     : NotificationController
 * author       : user
 * date         : 2026-01-28(수)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-28(수)                user            최초 생성
 */

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(@AuthenticationPrincipal Long userId) {
        List<Notification> notifications = notificationService.getMyNotifications(userId);

        List<NotificationResponse> response = new ArrayList<>();

        for (Notification notification : notifications) {
            response.add(NotificationResponse.from(notification));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(@AuthenticationPrincipal Long userId) {
        int findCount = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(findCount));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("단일 알림 읽음"));
    }
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("전체 알림 읽음"));
    }
}
