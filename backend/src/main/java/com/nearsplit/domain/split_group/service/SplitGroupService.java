package com.nearsplit.domain.split_group.service;

import com.nearsplit.domain.split_group.dto.SplitGroupRequest;
import com.nearsplit.domain.split_group.dto.SplitGroupResponse;
import com.nearsplit.domain.split_group.entity.GroupParticipant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.repository.GroupParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        SplitGroup newGroup = new SplitGroup();
        newGroup.setHostUserId(userId);
        newGroup.setTitle(request.getTitle());
        newGroup.setPickupLocation(request.getPickupLocation());
        newGroup.setTotalPrice(request.getTotalPrice());
        newGroup.setMaxParticipants(request.getMaxParticipants());
        newGroup.setClosedAt(request.getClosedAt());
        SplitGroup saved = splitGroupRepository.save(newGroup);
        GroupParticipant participant = new GroupParticipant();
        participant.setSplitGroup(saved);

        participantRepository.save(participant);

        return SplitGroupResponse.from(saved);
    }

    public List<SplitGroupResponse> getSHostedSplitGroups(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }

        List<SplitGroup> groupList = splitGroupRepository.findByHostUserId(userId);
        List<SplitGroupResponse> responseList = new ArrayList<>();
        for (SplitGroup group : groupList) {
            responseList.add(SplitGroupResponse.from(group));
        }

        return responseList;
    }
}
