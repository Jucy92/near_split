package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitGroupRepository extends JpaRepository<SplitGroup, Long>, SplitGroupRepositoryCustom {
    public List<SplitGroup> findByHostUserId(Long userId);      // 이게 단독이면 무슨 쓸모가 있지..? 내가 방장인거 다 찾는건가..

    Optional<SplitGroup> findByIdAndHostUserId(Long splitGroupId, Long userId);

    // 일단은 RECRUITING 중인 그룹 전체 조회
    //Page<SplitGroup> findByStatusOrderByCreatedAtDesc(SplitGroupStatus status);   // 네이밍 정렬 방법
    Page<SplitGroup> findByStatus(SplitGroupStatus status, Pageable pageable);

    // 상태로 그룹 조회 (Enum 사용)
    public List<SplitGroup> findByStatus(SplitGroupStatus status);


    boolean existsByIdAndHostUserId(Long groupId, Long userId);

    @Query(value = """
        SELECT * FROM split_group
        WHERE status = 'RECRUITING'
        AND location IS NOT NULL
        AND ST_DWithin(location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 4000)
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<SplitGroup> findNearByGroup(@Param("lat") double lat, @Param("lon") double lon);

}
