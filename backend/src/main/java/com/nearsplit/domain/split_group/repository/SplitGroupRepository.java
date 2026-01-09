package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.entity.SplitGroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitGroupRepository extends JpaRepository<SplitGroup, Long> {
    public List<SplitGroup> findByHostUserId(Long userId);      // 이게 단독이면 무슨 쓸모가 있지..? 내가 방장인거 다 찾는건가..

    Optional<SplitGroup> findByIdAndHostUserId(Long splitGroupId, Long userId);

    // 일단은 RECRUITING 중인 그룹 전체 조회
    //Page<SplitGroup> findByStatusOrderByCreatedAtDesc(SplitGroupStatus status);   // 네이밍 정렬 방법
    Page<SplitGroup> findByStatus(SplitGroupStatus status, Pageable pageable);

    // 상태로 그룹 조회
    public List<SplitGroup> findByStatus(String status);
    // 호스트 + 상태로 조회

}
