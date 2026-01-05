package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.GroupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
@Repository
public interface GroupParticipantRepository extends JpaRepository<GroupParticipant, Long> {

    List<GroupParticipant> findBySplitGroupId(Long splitGroupId);

    List<GroupParticipant> findByUserId(Long userId);

    @Query("SELECT p FROM GroupParticipant p " +
            "JOIN FETCH p.splitGroup " +  // ← Fetch Join
            "WHERE p.userId = :userId")
    List<GroupParticipant> findByUserIdWithGroup(@Param("userId") Long userId);
}
