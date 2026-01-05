package com.nearsplit.domain.split_group.service;

import com.nearsplit.domain.split_group.dto.SplitGroupRequest;
import com.nearsplit.domain.split_group.dto.SplitGroupResponse;
import com.nearsplit.domain.split_group.dto.SplitGroupSummaryResponse;
import com.nearsplit.domain.split_group.entity.GroupParticipant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.repository.GroupParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SplitGroupService {
    private final SplitGroupRepository splitGroupRepository;
    private final GroupParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Transactional
    public SplitGroupResponse createSplitGroup(Long userId, SplitGroupRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }
        // 새로운 그룹 생성
        SplitGroup newGroup = new SplitGroup();
        newGroup.setHostUserId(userId);
        newGroup.setTitle(request.getTitle());
        newGroup.setPickupLocation(request.getPickupLocation());
        newGroup.setTotalPrice(request.getTotalPrice());
        newGroup.setMaxParticipants(request.getMaxParticipants());
        newGroup.setClosedAt(request.getClosedAt());
        SplitGroup saved = splitGroupRepository.save(newGroup);
        
        // 새로운 그룹의 사용자 추가
        GroupParticipant participant = new GroupParticipant();
        participant.setSplitGroup(saved);
        participant.setUserId(userId);
        participant.setStatus("APPROVED");  // 방장(생성자 = 참여자) 인 경우라 바로 승인 처리
        participant.setShareAmount(saved.getTotalPrice()
                .divide(BigDecimal.valueOf(saved.getMaxParticipants()),2, RoundingMode.HALF_DOWN)); // 나누는 수, 소수점 2자리에서, 반올림

        participantRepository.save(participant);

        return SplitGroupResponse.from(saved);
    }

    public List<SplitGroupSummaryResponse> getMySplitGroups(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }

        List<GroupParticipant> participantGroups = participantRepository/*.findByUserId*/.findByUserIdWithGroup(userId);
        if (participantGroups == null) {
            throw new IllegalArgumentException("그룹에 참여한 정보가 없습니다.");
        }

        List<SplitGroupSummaryResponse> responses = new ArrayList<>();  // 본인이 관리장이 아닌 디테일->마스터 기본 정보(번호,제목,인원,금액 등)

        for (GroupParticipant participant : participantGroups) {
            responses.add(SplitGroupSummaryResponse.from(participant));
        }

        return responses;
    }
}
