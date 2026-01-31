package com.nearsplit.domain.split_group.dto;

import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import com.querydsl.core.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.nearsplit.domain.split_group.entity.QSplitGroup.splitGroup;
import static com.nearsplit.domain.user.entity.QUser.user;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Slf4j
public class SplitGroupResponse {
    // 그룹방 정보 (SplitGroup)
    private Long id;
    private String title;
    private BigDecimal totalPrice;
    private int maxParticipants;
    private String pickupLocation;
    private String pickupLocationGeo;
    private SplitGroupStatus groupState;
    private Long hostUserId;
    private String hostNickname;
    private int currentParticipants;
    private LocalDate closedAt; // 마감일
    private LocalDate createdAt;

    // 그룹방 참여자 정보(GroupParticipants)
    private List<ParticipantResponse> participants;

    public static SplitGroupResponse from(SplitGroup splitGroup) {

        return SplitGroupResponse.builder()
                .id(splitGroup.getId())
                .title(splitGroup.getTitle())
                .totalPrice(splitGroup.getTotalPrice())
                .maxParticipants(splitGroup.getMaxParticipants())
                .pickupLocation(splitGroup.getPickupLocation())
                .pickupLocationGeo(splitGroup.getPickupLocationGeo())
                .groupState(splitGroup.getStatus())
                .hostUserId(splitGroup.getHostUserId())
                .currentParticipants(splitGroup.getCurrentParticipants())
                .closedAt(splitGroup.getClosedAt())
                .createdAt(splitGroup.getCreatedAt().toLocalDate())
                .participants(splitGroup.getParticipants().stream()
                        //.map(participantEntity -> ParticipantResponse.from(participantEntity))    // 아래와 같이 축약 가능
                        .map(ParticipantResponse::from)
                        .toList())
                .build();
    }
    public static SplitGroupResponse from(Tuple tuple) {
        SplitGroup group = tuple.get(splitGroup);
        String nickname = tuple.get(user.nickname);

        return SplitGroupResponse.builder()
                .id(group.getId())
                .title(group.getTitle())
                .totalPrice(group.getTotalPrice())
                .maxParticipants(group.getMaxParticipants())
                .pickupLocation(group.getPickupLocation())
                .pickupLocationGeo(group.getPickupLocationGeo())
                .groupState(group.getStatus())
                .hostUserId(group.getHostUserId())
                .hostNickname(nickname)
                .currentParticipants(group.getCurrentParticipants())
                .closedAt(group.getClosedAt())
                .createdAt(group.getCreatedAt().toLocalDate())
                .participants(group.getParticipants().stream()
                        .map(ParticipantResponse::from)
                        .toList())
                .build();
    }
}
