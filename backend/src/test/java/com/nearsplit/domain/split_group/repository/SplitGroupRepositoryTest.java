package com.nearsplit.domain.split_group.repository;

import com.nearsplit.config.QueryDslConfig;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import lombok.extern.slf4j.Slf4j;
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
class SplitGroupRepositoryTest {
    @Autowired
    private SplitGroupRepository splitGroupRepository;

    @Test
    void 소분그룹_생성() {
        // given
        SplitGroup ng = SplitGroup.createGroup(
                1L, "쿠팡 양배추 소분", BigDecimal.valueOf(30_000),
                5, "수유역 4번 출구 옆 작업장", LocalDate.now().plusDays(7)
        );

        // when
        SplitGroup saved = splitGroupRepository.save(ng);
        log.info("소분 그룹 데이터 ={}",saved);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCurrentParticipants()).isEqualTo(0);  // 방장은 참여자 목록에 포함 안 됨
        assertThat(saved.getClosedAt()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(saved.getStatus()).isEqualTo(SplitGroupStatus.RECRUITING);  // Enum 비교
        assertThat(saved.getParticipants()).isEmpty();

    }

    @Test
    void 호스트로_그룹조회() {
        // given
        SplitGroup group1 = SplitGroup.createGroup(
                1L, "쿠팡 양배추 소분", BigDecimal.valueOf(30_000),
                5, "수유역 4번 출구 옆 작업장", LocalDate.now().plusDays(7)
        );
        SplitGroup group2 = SplitGroup.createGroup(
                1L, "쿠팡 버섯 소분", BigDecimal.valueOf(35_000),
                5, "수유역 4번 출구 옆 작업장", LocalDate.now().plusDays(5)
        );

        splitGroupRepository.save(group1);
        splitGroupRepository.save(group2);

        // when
        List<SplitGroup> groupList = splitGroupRepository.findByHostUserId(1L);

        // then
        assertThat(groupList).hasSize(2);

    }

    @Test
    void 상태로_그룹_조회() {
        // given
        SplitGroup recruiting = SplitGroup.builder()
                .hostUserId(1L)
                .title("모집중")
                .totalPrice(BigDecimal.valueOf(10000))
                .maxParticipants(3)
                .pickupLocation("강남")
                .status(SplitGroupStatus.RECRUITING)
                .build();

        SplitGroup closed = SplitGroup.builder()
                .hostUserId(2L)
                .title("종료")
                .totalPrice(BigDecimal.valueOf(20000))
                .maxParticipants(3)
                .pickupLocation("홍대")
                .status(SplitGroupStatus.CANCELLED)
                .build();

        splitGroupRepository.save(recruiting);
        splitGroupRepository.save(closed);

        // when
        List<SplitGroup> recruitingGroups = splitGroupRepository.findByStatus(SplitGroupStatus.RECRUITING);

        // then
        assertThat(recruitingGroups).hasSize(1);
        assertThat(recruitingGroups.get(0).getTitle()).isEqualTo("모집중");
    }

}
