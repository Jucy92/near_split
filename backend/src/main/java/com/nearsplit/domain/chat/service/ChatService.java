package com.nearsplit.domain.chat.service;

import com.nearsplit.common.exception.BusinessException;
import com.nearsplit.common.exception.ErrorCode;
import com.nearsplit.domain.chat.dto.ChatMessageRequest;
import com.nearsplit.domain.chat.entity.ChatMessage;
import com.nearsplit.domain.chat.repository.ChatMessageRepository;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    // 메시지 저장
    @Transactional
    public ChatMessage saveMessage(ChatMessageRequest request, Long senderId) {
        // 1. 그룹 참여자 확인
        if (!participantRepository.existsBySplitGroupIdAndUserId(request.getGroupId(), senderId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 2. 사용자 정보 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .groupId(request.getGroupId())
                .senderId(senderId)
                .senderName(sender.getName())
                .content(request.getContent())
                .type(request.getType())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("메시지 저장 완료: groupId={}, messageId={}", request.getGroupId(), saved.getId());

        return saved;
    }

    // 메시지 히스토리 조회 (페이징)
    public Page<ChatMessage> getMessageHistory(Long groupId, Long userId, Pageable pageable) {
        // 그룹 참여자 확인
        if (!participantRepository.existsBySplitGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return chatMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
    }

    // 최근 메시지 조회 (최대 50개)
    public List<ChatMessage> getRecentMessages(Long groupId, Long userId) {
        // 그룹 참여자 확인
        if (!participantRepository.existsBySplitGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return chatMessageRepository.findTop50ByGroupIdOrderByCreatedAtDesc(groupId);
    }
}
