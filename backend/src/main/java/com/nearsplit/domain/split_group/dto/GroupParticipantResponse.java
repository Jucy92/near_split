package com.nearsplit.domain.split_group.dto;

import com.nearsplit.domain.split_group.entity.SplitGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GroupParticipantResponse {

    private SplitGroup splitGroup;
    private Long participantUserId;
    private Integer quantity;
    private BigDecimal shareAmount;
    private String status;
    private LocalDateTime joinedAt;




}
