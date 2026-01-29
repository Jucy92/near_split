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
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Ref;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SplitGroupService {
    private final SplitGroupRepository splitGroupRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public SplitGroup createSplitGroup(Long userId, SplitGroupRequest request) {
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
        if (request.getClosedAt() != null) {
            if (!request.getClosedAt().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("마감일이 내일 이후로 설정해야 합니다.");
            }
        }
        newGroup.setClosedAt(request.getClosedAt());
        SplitGroup saved = splitGroupRepository.save(newGroup);
        log.info("생성된 그룹={}", saved);

        /*  // 무료나눔 선착순 2명! => 방장은 당연히 2명을 생각하지만, 실제로는 1명만 되는 문제가 있어서 방장 분리
        // 새로운 그룹의 사용자 추가
        Participant participant = new Participant();
        participant.setSplitGroup(saved);
        participant.setUserId(userId);
        participant.setStatus(ParticipantStatus.APPROVED);  // 방장(생성자 = 참여자) 인 경우라 바로 승인 처리
        participant.setShareAmount(saved.getTotalPrice()
                .divide(BigDecimal.valueOf(saved.getMaxParticipants()), 2, RoundingMode.HALF_DOWN)); // 나누는 수, 소수점 2자리에서, 반올림

        participantRepository.save(participant);
        log.info("생성된 그룹 사용자={}", participant);
        */

        return saved;
    }

    // 모집 중인 전체 그룹 조회
    public Page<SplitGroup> getAllRecruitingGroups(PageRequest pageRequest) {
        return splitGroupRepository.findByStatus(SplitGroupStatus.RECRUITING, pageRequest);
    }


    public List<Participant> getMySplitGroups(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다. 인증 정보를 확인해 주세요.");
        }

        List<Participant> participantGroups = participantRepository/*.findByUserId*/.findByUserIdWithGroup(userId);
        if (participantGroups == null) {
            throw new IllegalArgumentException("그룹에 참여한 정보가 없습니다.");
        }

        return participantGroups;
    }

    public SplitGroup getSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup group = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        /* // 볼 수 있어야 참여 신청을 하지..
        if (!participantRepository.existsBySplitGroupIdAndUserId(splitGroupId, userId)) {
            throw new IllegalArgumentException("권한이 없는 접근 입니다.");
        }
        */

        return group;

    }
    @Transactional
    public SplitGroup updateSplitGroup(Long splitGroupId, Long userId, SplitGroupRequest request) {
        SplitGroup target = splitGroupRepository.findByIdAndHostUserId(splitGroupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없습니다."));

        boolean hasParticipants = target.getCurrentParticipants() > 0;

        if (request.getTitle() != null) {
            if (hasParticipants) {
                throw new IllegalArgumentException("참여자가 있는 경우 제목 변경이 불가합니다.");
            }
            target.setTitle(request.getTitle());
        }
        if (request.getTotalPrice() != null) {
            if (hasParticipants) {
                throw new IllegalArgumentException("참여자가 있는 경우 총 금액이 불가합니다.");
            }
            target.setTotalPrice(request.getTotalPrice());
        }
        if (request.getMaxParticipants() != null) {
            if (hasParticipants) {
                throw new IllegalArgumentException("참여자가 있는 경우 총 인원 변경이 불가합니다.");
            }
            if (request.getMaxParticipants() < 1) {
                throw new IllegalArgumentException("모집 인원이 1명 이상이어야 합니다.");
            }
            target.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getPickupLocation() != null) target.setPickupLocation(request.getPickupLocation());
        if (request.getPickupLocationGeo() != null) target.setPickupLocationGeo(request.getPickupLocationGeo());

        if (request.getClosedAt() != null) {
            if (!request.getClosedAt().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("마감일이 내일 이후로 설정해야 합니다.");
            }
            target.setClosedAt(request.getClosedAt());
        }
        // 내용 수정하면 참여자한테 내용 변경 됐다고 알림 보내기?

        return target;
    }

    @Transactional
    public SplitGroup deleteSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup target = splitGroupRepository.findByIdAndHostUserId(splitGroupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 또는 수정 권한을 찾을 수 없습니다."));

        if (target.getStatus() != SplitGroupStatus.RECRUITING) {
            throw new IllegalArgumentException("모집 중인 상태에서만 삭제가 가능합니다.");
        }
        if (target.getCurrentParticipants() > 0) {
            throw new IllegalArgumentException("참여자가 있는 경우 삭제가 불가 합니다.");
        }

        target.setStatus(SplitGroupStatus.CANCELLED);
        splitGroupRepository.save(target);  // 삭제가 아닌 상태 변경인 것을 명시적으로 표시하기 위함
        return target;
    }


    @Transactional
    public Participant joinSplitGroup(Long splitGroupId, Long userId) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "존재하지 않는 그룹입니다."));

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

        notificationService.createNotification(findGroup.getHostUserId(), NotificationType.JOIN_REQUEST,
                "그룹 참여 요청", "그룹 참여 요청이 왔습니다.", findGroup.getId(), ReferenceType.SPLIT_GROUP);       // 리턴을 사용할 필요가 없구나..?
        log.info("findGroup.getClass().getName()={}", findGroup.getClass().getName());

        return participantRepository.save(joiner);
    }

    @Transactional
    public Participant cancelJoin(Long groupId, Long userId) {
        Participant participant = participantRepository.findBySplitGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참여 신청 내역이 없습니다."));

        if (ParticipantStatus.PENDING != participant.getStatus()) {
            throw new IllegalArgumentException("대기 중인 신청만 취소할 수 있습니다.");
        }

        participantRepository.delete(participant);

        // 알림 삭제 요청
        notificationService.deletedNotification(participant.getSplitGroup().getHostUserId(), groupId, ReferenceType.SPLIT_GROUP, NotificationType.JOIN_REQUEST);

        return participant;
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
        participant.setShareAmount(findGroup.getTotalPrice().divide(BigDecimal.valueOf(findGroup.getMaxParticipants() + 1), 2, RoundingMode.HALF_DOWN));

        notificationService.createNotification(participant.getUserId(), NotificationType.APPROVED,
                "그룹 참여 승인", "참여 신청이 승인 됐습니다.", splitGroupId, ReferenceType.SPLIT_GROUP);




        findGroup.setCurrentParticipants(findGroup.getCurrentParticipants() + 1);


        // 현재 인원 = 총 모집 인원 => 상태 풀로 변경
        if (findGroup.getMaxParticipants() == findGroup.getCurrentParticipants()) {
            findGroup.setStatus(SplitGroupStatus.FULL);
            for (Participant findParticipant : findGroup.getParticipants()) {
                notificationService.createNotification(findParticipant.getUserId(), NotificationType.GROUP_FULL,
                        "모집 완료", "모집이 완료 됐습니다.", splitGroupId, ReferenceType.SPLIT_GROUP);
            }
        }

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
        participantRepository.delete(participant);  // 삭제하면 위에 체크 로직도 불필요

        // participant.setStatus(ParticipantStatus.REJECTED); // 거절한 내역 남겨두려고 했는데, 재요청 시 로직 복잡해져서 일단 삭제로 변경


        notificationService.createNotification(participant.getUserId(), NotificationType.REJECTED,
                "그룹 참여 거절", "참여 신청이 거절 됐습니다.", splitGroupId, ReferenceType.SPLIT_GROUP);

        return participant;
    }

    public long getParticipantCount(Long splitGroupId, Long userId) {
        SplitGroup findGroup = splitGroupRepository.findById(splitGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));
        /*  // 리스트에서 타이틀 보고 조회해서 참여 인원 (3/5) 이런식으로 봐야할 수도 있으니깐... 일단 패스
        if (!participantRepository.existsBySplitGroupIdAndUserId(splitGroupId, userId)) {
            throw new IllegalArgumentException("참여자만 인원을 조회할 수 있습니다?");
        }
        */
        long count = participantRepository.countBySplitGroupIdAndStatus(splitGroupId, ParticipantStatus.APPROVED);
        return count;
    }
}
