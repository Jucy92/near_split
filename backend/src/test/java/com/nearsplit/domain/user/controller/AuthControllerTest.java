package com.nearsplit.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.JwtUtil;
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
import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    private RegisterRequest registerRequest;
    private Long userId;
    private String token;

    @BeforeEach
    void 설정() {
        registerRequest = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("테스터1")
                .nickname("사용자1")
                .build();

        userId = authService.register(registerRequest);

        token = jwtUtil.generateToken(userId);

    }

    @Test
    void 로그인_성공_쿠키_설정됨() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(jsonPath("$.userResponse.email").value("test123@test.com"))
                .andExpect(jsonPath("$.userResponse.nickname").value("닉네임1"));
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

    // ====================================================== 여기부터는 토큰 활용 ======================================================
    // ====================================================== 여기부터는 토큰 활용 ======================================================

    @Test
    void 프로필_조회_성공_쿠키() throws Exception {
        // given
        Cookie cookie = new Cookie("accessToken", token);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"));
    }
    @Test
    void 프로필_조회_성공_Authorization_헤더() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }


}