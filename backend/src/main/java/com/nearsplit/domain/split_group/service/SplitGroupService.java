package com.nearsplit.domain.split_group.service;

import com.nearsplit.domain.split_group.dto.*;
import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SplitGroupService {
    private final SplitGroupRepository splitGroupRepository;
    private final ParticipantRepository participantRepository;
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
        log.info("생성된 그룹={}",saved);

        // 새로운 그룹의 사용자 추가
        Participant participant = new Participant();
        participant.setSplitGroup(saved);
        participant.setUserId(userId);
        participant.setStatus(ParticipantStatus.APPROVED);  // 방장(생성자 = 참여자) 인 경우라 바로 승인 처리
        participant.setShareAmount(saved.getTotalPrice()
                .divide(BigDecimal.valueOf(saved.getMaxParticipants()),2, RoundingMode.HALF_DOWN)); // 나누는 수, 소수점 2자리에서, 반올림

        participantRepository.save(participant);
        log.info("그룹 사용자={}",participant);


        return SplitGroupResponse.from(saved);
    }

    public List<SplitGroupSummaryResponse> getMySplitGroups(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }

        List<Participant> participantGroups = participantRepository/*.findByUserId*/.findByUserIdWithGroup(userId);
        if (participantGroups == null) {
            throw new IllegalArgumentException("그룹에 참여한 정보가 없습니다.");
        }

        List<SplitGroupSummaryResponse> responses = new ArrayList<>();  // 본인이 관리장이 아닌 디테일->마스터 기본 정보(번호,제목,인원,금액 등)

        for (Participant participant : participantGroups) {
            responses.add(SplitGroupSummaryResponse.from(participant));
        }

        return responses;
    }

    public SplitGroupResponse getSplitGroup(Long splitGroupId, Long userId) {
        if (!participantRepository.existsBySplitGroupIdAndUserId(splitGroupId, userId)) {   // 해당 groupId에 userId가 참여자로 있는 지 체크
            throw new IllegalArgumentException("권한이 없는 접근 입니다.");
        }

        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("조회할 수 없는 그룹 번호입니다."));
        return SplitGroupResponse.from(findGroup);
    }

    @Transactional
    public void joinSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        if (participantRepository.existsBySplitGroupIdAndUserId(splitGroupId, userId)) {
            throw new IllegalArgumentException("이미 참여 신청한 그룹입니다.");
        }

        if (!SplitGroupStatus.RECRUITING.equals(findGroup.getStatus())) {
            throw new IllegalArgumentException("모집이 마감된 그룹입니다.");
        }

        if (findGroup.getCurrentParticipants() >= findGroup.getMaxParticipants()) {
            throw new IllegalArgumentException("정원이 마감되었습니다.");
        }

        Participant joiner = Participant.builder()
                .splitGroup(findGroup)
                .status(ParticipantStatus.PENDING)
                .userId(userId)
                .build();

        participantRepository.save(joiner);
    }

    @Transactional
    public Participant approveParticipant(Long splitGroupId, Long hostId, ParticipantActionRequest actionRequest) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        if (!findGroup.getHostUserId().equals(hostId)) {
            throw new IllegalArgumentException("방장만 승인할 수 있습니다.");
        }

        Participant participant = participantRepository.findBySplitGroupIdAndUserId(splitGroupId, actionRequest.getParticipantUserId())
                .orElseThrow(() -> new IllegalArgumentException("참여 신청 내역이 없습니다."));

        if (ParticipantStatus.APPROVED.equals(participant.getStatus())) {
            throw new IllegalArgumentException("이미 승인된 참여자입니다.");
        }

        participant.setStatus(ParticipantStatus.APPROVED);
        participant.setShareAmount(findGroup.getTotalPrice().divide(BigDecimal.valueOf(findGroup.getMaxParticipants()),2,RoundingMode.HALF_DOWN));

        findGroup.setCurrentParticipants(findGroup.getCurrentParticipants()+1);

        return participant;

    }
    @Transactional
    public Participant rejectedParticipant(Long splitGroupId, Long hostId, ParticipantActionRequest actionRequest) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        if (!findGroup.getHostUserId().equals(hostId)) {
            throw new IllegalArgumentException("방장만 거절할 수 있습니다.");
        }

        Participant participant = participantRepository.findBySplitGroupIdAndUserId(splitGroupId, actionRequest.getParticipantUserId())
                .orElseThrow(() -> new IllegalArgumentException("참여 신청 내역이 없습니다."));

        if (ParticipantStatus.REJECTED.equals(participant.getStatus())) {
            throw new IllegalArgumentException("이미 거절된 참여자입니다.");
        }

        participant.setStatus(ParticipantStatus.REJECTED);

        return participant;

    }


}
