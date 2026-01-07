package com.nearsplit.domain.split_group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * packageName  : com.nearsplit.domain.split_group.dto
 * fileName     : ParticipantActionRequest
 * author       : user
 * date         : 2026-01-07(수)
 * description   : 그룹 참여자에 대한 처리(승인,거절) 정보 DTO
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-07(수)         jcy             최초 생성
 */

@Getter @Setter
public class ParticipantActionRequest {
    @NotNull
    private Long participantUserId;
    private String reason;      // 거절인 경우에만 이유
    // splitGroupId는 pathVariable 로 넘어옴
}
