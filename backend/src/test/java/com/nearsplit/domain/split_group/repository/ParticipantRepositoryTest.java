package com.nearsplit.domain.split_group.repository;

import com.nearsplit.config.QueryDslConfig;
import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)  // QueryDSL의 JPAQueryFactory 빈 로드
@Transactional
@Slf4j
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository groupParticipantRepository;
    @Autowired
    private SplitGroupRepository splitGroupRepository;

    private SplitGroup splitGroup;

    @BeforeEach
    void 소분그룹생성() {
        SplitGroup group = SplitGroup.createGroup(
                1L, "쿠팡 양배추 소분", BigDecimal.valueOf(30_000),
                5, "수유역 4번 출구 옆 작업장", LocalDate.now().plusDays(7)
        );

        splitGroup = splitGroupRepository.save(group);
    }

    @Test
    void 그룹_참여자생성() {
        // given
        Participant participant = Participant.builder()
                .splitGroup(splitGroup)
                .quantity(2)
                .userId(2L)
                .shareAmount(BigDecimal.valueOf(6_000))
                .build();

        // when
        Participant saved = groupParticipantRepository.save(participant);
        log.info("생성된 그룹 정보={}",splitGroup);
        log.info("그룹 참여자 정보={}",participant);
        log.info("참여자 추가 후={}",splitGroup);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ParticipantStatus.PENDING); // 기본값 (Enum 비교)
        assertThat(saved.getSplitGroup().getId()).isEqualTo(splitGroup.getId());  // splitGroup ID와 비교
        assertThat(saved.getJoinedAt()).isNotNull();
    }
    @Test
    void 그룹_모든_참여자_조회() {
        // given
        Participant participant1 = Participant.builder()
                .splitGroup(splitGroup)
                .quantity(2)
                .userId(2L)
                .shareAmount(BigDecimal.valueOf(6_000))
                .build();
        Participant participant2 = Participant.builder()
                .splitGroup(splitGroup)
                .quantity(2)
                .userId(3L)
                .shareAmount(BigDecimal.valueOf(6_000))
                .build();

        // when
        Participant saved1 = groupParticipantRepository.save(participant1);
        Participant saved2 = groupParticipantRepository.save(participant2);
        log.info("생성된 그룹 정보={}",splitGroup);

        List<Participant> participants = groupParticipantRepository.findBySplitGroupId(splitGroup.getId());
        log.info("그룹 번호로 참여자 조회={}",participants);

        // then
        assertThat(participants).hasSize(2);
    }
}
