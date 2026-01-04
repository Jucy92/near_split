package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.GroupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface GroupParticipantRepository extends JpaRepository<GroupParticipant, Long> {

    List<GroupParticipant> findBySplitGroupId(Long splitGroupId);
}
