package com.nearsplit.domain.chat.dto;

import com.nearsplit.domain.chat.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    @NotNull(message = "그룹 ID는 필수입니다")
    private Long groupId;

    @NotBlank(message = "메시지 내용은 필수입니다")
    private String content;

    @NotNull(message = "메시지 타입은 필수입니다")
    private ChatMessage.MessageType type;
}
