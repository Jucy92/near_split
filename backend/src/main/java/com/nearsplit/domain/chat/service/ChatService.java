package com.nearsplit.domain.chat.service;

import com.nearsplit.common.exception.BusinessException;
import com.nearsplit.common.exception.ErrorCode;
import com.nearsplit.domain.chat.dto.ChatMessageRequest;
import com.nearsplit.domain.chat.entity.ChatMessage;
import com.nearsplit.domain.chat.repository.ChatMessageRepository;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SplitGroupRepository splitGroupRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // 메시지 저장
    @Transactional
    public ChatMessage saveMessage(ChatMessageRequest request, Long senderId) {
        // 1. 그룹 참여자 확인
        SplitGroup group = splitGroupRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));     // 권한 없는 알림은 뜨는데 해당화면이 꺼지지 않아서 조회는 가능..

        boolean isHost = group.getHostUserId().equals(senderId);
        boolean isParticipant = participantRepository.existsBySplitGroupIdAndUserId(request.getGroupId(), senderId);

        if (!isHost && !isParticipant) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 2. 사용자 정보 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .groupId(request.getGroupId())
                .senderId(senderId)
                .senderName(sender.getNickname())
                .content(request.getContent())
                .type(request.getType())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("메시지 저장 완료: groupId={}, messageId={}", request.getGroupId(), saved.getId());
        simpMessagingTemplate.convertAndSend("/topic/notification/"+ "사용자들...","내용...");    // 근데 여기서 포문 돌면서 다 찾아..?

        return saved;
    }

    // 메시지 히스토리 조회 (페이징)
    public Page<ChatMessage> getMessageHistory(Long groupId, Long userId, Pageable pageable) {
        // 그룹 참여자 확인
        SplitGroup group = splitGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));     // 권한 없는 알림은 뜨는데 해당화면이 꺼지지 않아서 조회는 가능..

        boolean isHost = group.getHostUserId().equals(userId);
        boolean isParticipant = participantRepository.existsBySplitGroupIdAndUserId(groupId, userId);

        if (!isHost && !isParticipant) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return chatMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
    }

    // 최근 메시지 조회 (최대 50개)
    public List<ChatMessage> getRecentMessages(Long groupId, Long userId) {
        // 그룹 참여자 확인
        SplitGroup group = splitGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        boolean isHost = group.getHostUserId().equals(userId);
        boolean isParticipant = participantRepository.existsBySplitGroupIdAndUserId(groupId, userId);

        if (!isHost && !isParticipant) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return chatMessageRepository.findTop50ByGroupIdOrderByCreatedAtDesc(groupId);
    }
}
