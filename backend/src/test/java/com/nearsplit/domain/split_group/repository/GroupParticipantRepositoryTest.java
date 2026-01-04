package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.GroupParticipant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@Slf4j
class GroupParticipantRepositoryTest {

    @Autowired
    private GroupParticipantRepository groupParticipantRepository;
    @Autowired
    private SplitGroupRepository splitGroupRepository;

    private SplitGroup splitGroup;

    @BeforeEach
    void 소분그룹생성() {
        SplitGroup group = new SplitGroup();
        group.setHostUserId(1L);
        group.setTitle("쿠팡 양배추 소분");
        group.setTotalPrice(BigDecimal.valueOf(30_000));
        group.setMaxParticipants(5);
        group.setPickupLocation("수유역 4번 출구 옆 작업장");
        group.setClosedAt(LocalDate.now().plusDays(7));

        splitGroup = splitGroupRepository.save(group);
    }

    @Test
    void 그룹_참여자생성() {
        // given
        GroupParticipant participant = new GroupParticipant();
        participant.setSplitGroup(splitGroup);
        participant.setQuantity(2);
        participant.setUserId(2L);
        participant.setShareAmount(BigDecimal.valueOf(6_000));

        // when
        GroupParticipant saved = groupParticipantRepository.save(participant);
        log.info("생성된 그룹 정보={}",splitGroup);
        log.info("그룹 참여자 정보={}",participant);
        log.info("참여자 추가 후={}",splitGroup);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("PENDING"); // 기본값
        assertThat(saved.getSplitGroup().getId()).isEqualTo(participant.getId());
        assertThat(saved.getJoinedAt()).isNotNull();
    }
    @Test
    void 그룹_모든_참여자_조회() {
        // given
        GroupParticipant participant1 = new GroupParticipant();
        participant1.setSplitGroup(splitGroup);
        participant1.setQuantity(2);
        participant1.setUserId(2L);
        participant1.setShareAmount(BigDecimal.valueOf(6_000));
        GroupParticipant participant2 = new GroupParticipant();
        participant2.setSplitGroup(splitGroup);
        participant2.setQuantity(2);
        participant2.setUserId(3L);
        participant2.setShareAmount(BigDecimal.valueOf(6_000));

        // when
        GroupParticipant saved1 = groupParticipantRepository.save(participant1);
        GroupParticipant saved2 = groupParticipantRepository.save(participant2);
        log.info("생성된 그룹 정보={}",splitGroup);

        List<GroupParticipant> participants = groupParticipantRepository.findBySplitGroupId(splitGroup.getId());
        log.info("그룹 번호로 참여자 조회={}",participants);

        // then
        assertThat(participants).hasSize(2);
    }
}