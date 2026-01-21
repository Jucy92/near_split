package com.nearsplit.domain.chat.controller;

import com.nearsplit.common.dto.ApiResponse;
import com.nearsplit.domain.chat.dto.ChatMessageRequest;
import com.nearsplit.domain.chat.dto.ChatMessageResponse;
import com.nearsplit.domain.chat.entity.ChatMessage;
import com.nearsplit.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * WebSocket 메시지 전송
     * 클라이언트 → /app/chat/{groupId}/send
     * 서버 → /topic/chat/{groupId} 로 브로드캐스트
     */
    @MessageMapping("/chat/{groupId}/send")     // 클라이언트가 메시지 보낼 목적지
    @SendTo("/topic/chat/{groupId}")            // 서버가 응답 브로드캐스트할 목적지 (Broker 에서 관리)
    public ChatMessageResponse sendMessage(@DestinationVariable Long groupId,   // @DestinationVariable(STOMP) == @PathVariable(HTTP) 동일한 역할 
                                           @Valid ChatMessageRequest request,
                                           SimpMessageHeaderAccessor headerAccessor) {  // WebSocket 세션 접근

        // WebSocket 세션에서 사용자 ID 추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        log.info("메시지 수신: groupId={}, userId={}, content={}",
            groupId, userId, request.getContent());

        // 메시지 저장
        ChatMessage message = chatService.saveMessage(request, userId);

        // 모든 구독자에게 브로드캐스트
        return ChatMessageResponse.from(message);
    }

    /**
     * REST API: 메시지 히스토리 조회
     * GET /api/chat/{groupId}/history
     */
    @GetMapping("/api/chat/{groupId}/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessageHistory(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = chatService.getMessageHistory(groupId, userId, pageable);
        Page<ChatMessageResponse> responsePage = messages.map(ChatMessageResponse::from);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * REST API: 최근 메시지 조회
     * GET /api/chat/{groupId}/recent
     */
    @GetMapping("/api/chat/{groupId}/recent")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getRecentMessages(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId) {

        List<ChatMessage> messages = chatService.getRecentMessages(groupId, userId);
        List<ChatMessageResponse> responses = messages.stream()
                .map(ChatMessageResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
