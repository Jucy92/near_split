package com.nearsplit.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.security.JwtUtil;
import com.nearsplit.domain.user.dto.RegisterRequest;
import com.nearsplit.domain.user.dto.UserUpdateRequest;
import com.nearsplit.domain.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {  //  JWT 인증 테스트
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
    void 설정() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("사용자1")
                .nickname("테스터1")
                .build();

        userId = authService.register(registerRequest);
        token = jwtUtil.generateToken(userId);
    }

    @Test
    void 프로필_조회_성공_Authorization_헤더() throws Exception {
        // given
        // @BeforeEach 설정

        // when & then
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test1@test.com"))
                .andExpect(jsonPath("$.name").value("사용자1"))
                .andExpect(jsonPath("$.nickname").value("테스터1"));
    }

    @Test
    void 프로필_조회_성공_쿠키() throws Exception {
        // given
        Cookie cookie = new Cookie("accessToken", token);   // 이렇게 안넣어주고, 로그인 로직 호출해서 거기서 직접 넣게 하는 방식으로 해도 됨

        // when & then      토큰 생성은 설정에서 했고, 보내지는 로직은 없지만 보냈다는 가정하에 지금 request를 다시 받는 중임
        mockMvc.perform(get("/api/users/me")/*.header("Authorization", "Bearer " + token)*/
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test1@test.com"))
                .andExpect(jsonPath("$.name").value("사용자1"))
                .andExpect(jsonPath("$.nickname").value("테스터1"));

    }

    @Test
    void 프로필_조회_실패_토큰_없음() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 프로필_조회_실패_잘못된_토큰() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 프로필_수정_성공() throws Exception {
        // given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname("새닉네임")
                .address("서울시 강남구")
                .location("강남역")
                .build();


        // when & then
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.email").value("test1@test.com"));
    }

    @Test
    void 프로필_수정_실패_중복닉네임() throws Exception {
        // given
        RegisterRequest register = RegisterRequest.builder()
                .email("another@test.com")
                .password("test1234")
                .name("사용자2")
                .nickname("다른닉네임")
                .build();
        authService.register(register);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname("다른닉네임")
                .address("서울시 강남구")
                .location("강남역")
                .build();

        Cookie cookie = new Cookie("accessToken", token);

        // when & then
        mockMvc.perform(patch("/api/users/me")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());


    }

    @Test
    void 프로필_수정_성공_중복닉네임_본인() throws Exception {
        // given
        RegisterRequest register = RegisterRequest.builder()
                .email("another@test.com")
                .password("test1234")
                .name("사용자2")
                .nickname("다른닉네임")
                .build();
        authService.register(register);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
//                .nickname("다른닉네임")
                .nickname("테스터1")
                .address("서울시 강남구")
                .location("강남역")
                .build();

        Cookie cookie = new Cookie("accessToken", token);

        // when & then
        mockMvc.perform(patch("/api/users/me")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("테스터1"));

    }


}