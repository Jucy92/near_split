package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findBySplitGroupId(Long splitGroupId);

    List<Participant> findByUserId(Long userId);

    @Query("SELECT p FROM Participant p " +
            "JOIN FETCH p.splitGroup " +  // ← Fetch Join
            "WHERE p.userId = :userId")
    List<Participant> findByUserIdWithGroup(@Param("userId") Long userId);

    Optional<Participant> findBySplitGroupIdAndUserId(Long splitGroupId, Long userId);

    boolean existsBySplitGroupIdAndUserId(Long splitGroupId, Long userId);

    long countBySplitGroupIdAndStatus(Long splitGroupId, ParticipantStatus status);
}
