package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.SplitGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitGroupRepository extends JpaRepository<SplitGroup, Long> {
    public List<SplitGroup> findByHostUserId(Long hostUserId);

    // 상태로 그룹 조회
    public List<SplitGroup> findByStatus(String status);
    // 호스트 + 상태로 조회

}
