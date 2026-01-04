package com.nearsplit.domain.split_group.dto;

import com.nearsplit.domain.split_group.entity.SplitGroup;
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
    private String groupState;
    private LocalDate closedAt; // 마감일
    private LocalDate createdAt;

    // 그룹방 참여자 정보(GroupParticipants)
    private List<GroupParticipantResponse> participants;
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

                //.participantUserId(splitGroup.getParticipants().stream().filter())    // anyMatch가 아닌가..? 그걸로 userId 찾아서 그 정보 가져올랬는데..
                .build();
    }
}
