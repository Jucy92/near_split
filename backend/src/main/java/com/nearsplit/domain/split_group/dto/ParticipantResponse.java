package com.nearsplit.domain.split_group.dto;

import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
@AllArgsConstructor
public class ParticipantResponse {

    //private SplitGroup splitGroup;    // 순환 참조 위험
    private Long id;
//    private Long splitGroupId;
    private Long userId;
    private Integer quantity;
    private BigDecimal shareAmount;
    private ParticipantStatus status;              // PENDING, APPROVED, REJECTED
    private LocalDateTime joinedAt;


    public static ParticipantResponse from(Participant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
//                .splitGroupId(participant.getSplitGroup().getId())
                .userId(participant.getUserId())
                .quantity(participant.getQuantity())
                .shareAmount(participant.getShareAmount())
                .status(participant.getStatus())
                .joinedAt(participant.getJoinedAt())
                .build();
    }

}
