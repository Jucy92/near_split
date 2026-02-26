package com.nearsplit.domain.split_group.service;

import com.nearsplit.common.exception.BusinessException;
import com.nearsplit.common.exception.ErrorCode;
import com.nearsplit.domain.notification.entity.NotificationType;
import com.nearsplit.domain.notification.entity.ReferenceType;
import com.nearsplit.domain.notification.service.NotificationService;
import com.nearsplit.domain.split_group.dto.*;
import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SplitGroupService {
    private final SplitGroupRepository splitGroupRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ========================================
    // 그룹 생성
    // ========================================
    @Transactional
    public SplitGroup createSplitGroup(Long userId, SplitGroupRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }

        // 도메인 정적 팩토리로 그룹 생성 (마감일 검증 포함)
        SplitGroup newGroup = SplitGroup.createGroup(
                userId,
                request.getTitle(),
                request.getTotalPrice(),
                request.getMaxParticipants(),
                request.getPickupLocation(),
                request.getClosedAt()
        );

        SplitGroup saved = splitGroupRepository.save(newGroup);
        log.info("생성된 그룹={}", saved);
        return saved;
    }

    // ========================================
    // 그룹 조회
    // ========================================

    // 모집 중인 전체 그룹 조회
    public Page<SplitGroup> getAllRecruitingGroups(PageRequest pageRequest) {
        return splitGroupRepository.findByStatus(SplitGroupStatus.RECRUITING, pageRequest);
    }

    public List<Participant> getMySplitGroups(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }

        List<Participant> participantGroups = participantRepository.findByUserIdWithGroup(userId);
        if (participantGroups == null) {
            throw new IllegalArgumentException("그룹에 참여한 정보가 없습니다.");
        }

        return participantGroups;
    }

    public SplitGroup getSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup group = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 참여자들 닉네임 조회 후 세팅
        List<Long> userIds = group.getParticipants().stream().map(Participant::getUserId).toList();
        userIds = new ArrayList<>(userIds);
        userIds.add(group.getHostUserId());

        Map<Long, String> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        group.getParticipants().forEach(p -> p.assignNickname(userMap.get(p.getUserId())));

        return group;
    }

    // ========================================
    // 그룹 수정
    // ========================================
    @Transactional
    public SplitGroup updateSplitGroup(Long splitGroupId, Long userId, SplitGroupRequest request) {
        SplitGroup target = splitGroupRepository.findByIdAndHostUserId(splitGroupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없습니다."));

        // 도메인 메서드로 수정 (참여자 유무 검증 포함)
        target.updateGroup(
                request.getTitle(),
                request.getTotalPrice(),
                request.getMaxParticipants(),
                request.getPickupLocation(),
                request.getPickupLocationGeo(),
                request.getClosedAt()
        );

        return target;
    }

    // ========================================
    // 그룹 삭제 (Soft Delete)
    // ========================================
    @Transactional
    public SplitGroup deleteSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup target = splitGroupRepository.findByIdAndHostUserId(splitGroupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 또는 수정 권한을 찾을 수 없습니다."));

        // 도메인 메서드로 취소 (상태 + 참여자 검증 포함)
        target.cancel();
        splitGroupRepository.save(target);
        return target;
    }

    // ========================================
    // 참여 신청
    // ========================================
    @Transactional
    public Participant joinSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "존재하지 않는 그룹입니다."));

        if (participantRepository.existsBySplitGroupIdAndUserId(splitGroupId, userId)) {
            throw new IllegalArgumentException("이미 참여 신청한 그룹입니다.");
        }

        // 도메인 메서드로 참여 가능 여부 검증
        findGroup.validateCanJoin();

        Participant joiner = Participant.builder()
                .splitGroup(findGroup)
                .status(ParticipantStatus.PENDING)
                .userId(userId)
                .build();

        // 알림 발송 (인프라 관심사 → 서비스 책임)
        notificationService.createNotification(findGroup.getHostUserId(), NotificationType.JOIN_REQUEST,
                "그룹 참여 요청", "그룹 참여 요청이 왔습니다.", findGroup.getId(), ReferenceType.SPLIT_GROUP);

        return participantRepository.save(joiner);
    }

    // ========================================
    // 참여 취소
    // ========================================
    @Transactional
    public Participant cancelJoin(Long groupId, Long userId) {
        Participant participant = participantRepository.findBySplitGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참여 신청 내역이 없습니다."));

        // 도메인 메서드로 취소 가능 여부 검증
        participant.validateCancellable();

        participantRepository.delete(participant);

        // 알림 삭제 (인프라 관심사 → 서비스 책임)
        notificationService.deletedNotification(participant.getSplitGroup().getHostUserId(),
                groupId, ReferenceType.SPLIT_GROUP, NotificationType.JOIN_REQUEST);

        return participant;
    }

    // ========================================
    // 참여자 승인
    // ========================================
    @Transactional
    public Participant approveParticipant(Long splitGroupId, Long hostId, ParticipantActionRequest actionRequest) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        // 도메인 메서드로 방장 검증
        findGroup.validateHost(hostId);

        Participant participant = participantRepository.findBySplitGroupIdAndUserId(splitGroupId, actionRequest.getParticipantUserId())
                .orElseThrow(() -> new IllegalArgumentException("참여 신청 내역이 없습니다."));

        // 도메인 메서드로 승인 처리 (분담금 계산 + 인원 증가 + FULL 전환)
        boolean becameFull = findGroup.approveParticipant(participant);

        // 알림 발송 (인프라 관심사 → 서비스 책임)
        notificationService.createNotification(participant.getUserId(), NotificationType.APPROVED,
                "그룹 참여 승인", "참여 신청이 승인 됐습니다.", splitGroupId, ReferenceType.SPLIT_GROUP);

        // 정원 도달 시 전체 참여자에게 모집 완료 알림
        if (becameFull) {
            for (Participant findParticipant : findGroup.getParticipants()) {
                notificationService.createNotification(findParticipant.getUserId(), NotificationType.GROUP_FULL,
                        "모집 완료", "모집이 완료 됐습니다.", splitGroupId, ReferenceType.SPLIT_GROUP);
            }
        }

        return participant;
    }

    // ========================================
    // 참여자 거절
    // ========================================
    @Transactional
    public Participant rejectedParticipant(Long splitGroupId, Long hostId, ParticipantActionRequest actionRequest) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        // 도메인 메서드로 방장 검증
        findGroup.validateHost(hostId);

        Participant participant = participantRepository.findBySplitGroupIdAndUserId(splitGroupId, actionRequest.getParticipantUserId())
                .orElseThrow(() -> new IllegalArgumentException("참여 신청 내역이 없습니다."));

        participantRepository.delete(participant);

        // 알림 발송 (인프라 관심사 → 서비스 책임)
        notificationService.createNotification(participant.getUserId(), NotificationType.REJECTED,
                "그룹 참여 거절", "참여 신청이 거절 됐습니다.", splitGroupId, ReferenceType.SPLIT_GROUP);

        return participant;
    }

    // ========================================
    // 참여자 수 조회
    // ========================================
    public long getParticipantCount(Long splitGroupId, Long userId) {
        splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        return participantRepository.countBySplitGroupIdAndStatus(splitGroupId, ParticipantStatus.APPROVED);
    }
}
