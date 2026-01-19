package com.nearsplit.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nearsplit.common.security.JwtUtil;
import com.nearsplit.domain.user.dto.RegisterRequest;
import com.nearsplit.domain.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * packageName  : com.nearsplit.common.exception
 * fileName     : GlobalExceptionHandlerTest
 * author       : user
 * date         : 2026-01-19(월)
 * description   : 예외처리 테스트 케이스
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-19(월)                user            최초 생성
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    private Long userId;
    private String token;



    @BeforeEach
    void 초기화() {
        RegisterRequest register = RegisterRequest.builder()
                .email("test1@test.com").password("test1234").name("호스트").nickname("하늘다람쥐").build();
        userId = authService.register(register);
        token = jwtUtil.generateToken(userId);

    }

    @Test
    void 예외처리_비즈니스() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(get("/api/split/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("G001"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 그룹입니다"));
    }

}