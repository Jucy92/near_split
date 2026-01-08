package com.nearsplit.domain.split_group.dto;

import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SplitGroupResponse {
    // 그룹방 정보 (SplitGroup)
    private Long SplitGroupId;
    private String title;
    private BigDecimal totalPrice;
    private int maxParticipants;
    private String pickupLocation;
    private String pickupLocationGeo;
    private SplitGroupStatus groupState;
    private LocalDate closedAt; // 마감일
    private LocalDate createdAt;

    // 그룹방 참여자 정보(GroupParticipants)
    private List<ParticipantResponse> participants;

    public static SplitGroupResponse from(SplitGroup splitGroup) {

        return SplitGroupResponse.builder()
                .SplitGroupId(splitGroup.getId())
                .title(splitGroup.getTitle())
                .totalPrice(splitGroup.getTotalPrice())
                .maxParticipants(splitGroup.getMaxParticipants())
                .pickupLocation(splitGroup.getPickupLocation())
                .pickupLocationGeo(splitGroup.getPickupLocationGeo())
                .groupState(splitGroup.getStatus())
                .closedAt(splitGroup.getClosedAt())
                .createdAt(splitGroup.getCreatedAt().toLocalDate())
                .participants(splitGroup.getParticipants().stream()
                        //.map(participantEntity -> ParticipantResponse.from(participantEntity))    // 아래와 같이 축약 가능
                        .map(ParticipantResponse::from)
                        .toList())
                .build();
    }
}
