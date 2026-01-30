package com.nearsplit.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.security.JwtUtil;
import com.nearsplit.domain.user.dto.LoginRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest         // 테스트를 위해 Spring Boot 전체 컨텍스트를 로드하여 모든 빈 등록
@AutoConfigureMockMvc   // MockMvc를 자동으로 설정
@Transactional
class AuthControllerTest {      // 로그인 및 쿠키 테스트
    @Autowired          // 여기에 주입
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    private RegisterRequest registerRequest;

    @BeforeEach
    void 설정() {
        registerRequest = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("테스터1")
                .nickname("사용자1")
                .build();

        authService.register(registerRequest);

    }

    @Test
    void 로그인_성공_쿠키_설정됨() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login") // 프론트에서 데이터 던져주는 것처럼, method, headers, body 담아서 보내는 형식
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // 이미 이런 일들이 모두 끝난 상태:
                // ✅ Controller 실행 완료
                // ✅ Service 실행 완료
                // ✅ 토큰 생성 완료
                // ✅ 쿠키 설정 완료
                // ✅ 응답 생성 완료
                // ✅ ResultActions에 응답 저장 완료
                .andExpect(status().isOk())     // ← 저장된 응답 확인 시작
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(jsonPath("$.data.userResponse.email").value("test1@test.com"))
                .andExpect(jsonPath("$.data.userResponse.nickname").isNotEmpty());  // 닉네임 자동 생성됨
    }

    @Test
    void 로그인_실패_잘못된_비밀번호() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test1@test.com")
                .password("wrongpassword")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 로그인_실패_존재하지_않는_이메일() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("test1234")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

}