package com.nearsplit.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Spring 없이 직접 생성
        String secret = "nearsplit-secret-key-change-this-in-production-must-be-at-least-256-bits";
        int expiration = 86400000;
        jwtUtil = new JwtUtil(secret, expiration);
    }

    @Test
    void JWT_생성_및_검증() {
        // given
        Long userId = 999L;

        // when
        String token = jwtUtil.generateToken(userId);

        // then
        System.out.println("생성된 토큰: " + token);
        assertThat(token).isNotNull();
        assertThat(jwtUtil.validToken(token)).isEqualTo(TokenStatus.VALID);
        assertThat(jwtUtil.getUserId(token)).isEqualTo(userId);

        System.out.println("✅ 토큰 검증 성공!");
        System.out.println("추출된 userId: " + jwtUtil.getUserId(token));
    }
}
