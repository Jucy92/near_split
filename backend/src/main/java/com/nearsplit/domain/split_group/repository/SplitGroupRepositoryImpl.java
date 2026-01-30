package com.nearsplit.domain.split_group.repository;

import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.nearsplit.domain.split_group.entity.QSplitGroup.splitGroup;
import static com.nearsplit.domain.user.entity.QUser.user;

/**
 * packageName  : com.nearsplit.domain.split_group.repository
 * fileName     : SplitGroupRepositoryImpl
 * author       : user
 * date         : 2026-01-29(목)
 * description   : SplitGroupRepositoryCustom 인터페이스 구현체
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-29(목)                user            최초 생성
 */

@RequiredArgsConstructor
public class SplitGroupRepositoryImpl implements SplitGroupRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Tuple> findSplitGroupWithNickname(Long groupId) {
        Tuple result = queryFactory
                .select(splitGroup, user.nickname)
                .from(splitGroup)
                .leftJoin(user).on(splitGroup.hostUserId.eq(user.id))
                .where(splitGroup.id.eq(groupId))
                .fetchFirst();
        return Optional.ofNullable(result);

    }
}
