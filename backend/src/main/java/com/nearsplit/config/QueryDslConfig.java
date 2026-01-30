package com.nearsplit.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName  : com.nearsplit.config
 * fileName     : QueryDslConfig
 * author       : user
 * date         : 2026-01-29(목)
 * description   : queryDSL 설정 파일
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-29(목)                user            최초 생성
 */
@Configuration
public class QueryDslConfig {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
