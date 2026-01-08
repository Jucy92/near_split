package com.nearsplit.domain.split_group.dto;

import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * packageName  : com.nearsplit.domain.split_group.dto
 * fileName     : SplitGroupSummaryResponse
 * author       : jcy
 * date         : 2026-01-05(월)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-05(월)          jcy            최초 생성
 */

@Getter
@Builder
@AllArgsConstructor
public class SplitGroupSummaryResponse {
    private Long groupId;  // id → groupId (명확하게)
    private String title;
    private BigDecimal totalPrice;
    private Integer currentParticipants;  // 추가
    private Integer maxParticipants;
    private SplitGroupStatus status;      // 추가
    private LocalDate closedAt;           // 추가
    private Boolean isHost;               // 추가 (방장 여부)

    public static SplitGroupSummaryResponse from(Participant participant) {     // 호스트 유무 체크하기 위해서 파티원 엔티티 받음
        SplitGroup splitGroup = participant.getSplitGroup();

        return SplitGroupSummaryResponse.builder()
                .groupId(splitGroup.getId())
                .title(splitGroup.getTitle())
                .totalPrice(splitGroup.getTotalPrice())
                .currentParticipants(splitGroup.getCurrentParticipants())
                .maxParticipants(splitGroup.getMaxParticipants())
                .status(splitGroup.getStatus())
                .closedAt(splitGroup.getClosedAt())
                .isHost(participant.getUserId().equals(splitGroup.getHostUserId()))
                .build();
    }
}
