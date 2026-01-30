package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.querydsl.core.Tuple;

import java.util.Optional;

/**
 * packageName  : com.nearsplit.domain.split_group.repository
 * fileName     : SplitGroupRepositoryCustom
 * author       : user
 * date         : 2026-01-29(목)
 * description   : SplitGroupRepository 에서 상속 받을 QueryDSL 인터페이스
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-29(목)                user            최초 생성
 */

public interface SplitGroupRepositoryCustom {
    Optional<Tuple> findSplitGroupWithNickname(Long groupId);
}

/*
SELECT
    B.*
FROM
     SPLIT_GROUP A
JOIN
     PARTICIPANT B ON A.ID = B.SPLIT_GROUP_ID
JOIN
     USER C ON B.USER_ID = C.ID
 */