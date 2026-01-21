package com.nearsplit.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nearsplit.domain.chat.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageResponse {

    private Long messageId;
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String content;
    private ChatMessage.MessageType type;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .groupId(message.getGroupId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .type(message.getType())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
