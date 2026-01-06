package com.nearsplit.domain.split_group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.JwtUtil;
import com.nearsplit.domain.split_group.dto.SplitGroupRequest;
import com.nearsplit.domain.split_group.repository.GroupParticipantRepository;
import com.nearsplit.domain.split_group.service.SplitGroupService;
import com.nearsplit.domain.user.dto.LoginRequest;
import com.nearsplit.domain.user.dto.RegisterRequest;
import com.nearsplit.domain.user.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.print.attribute.standard.Media;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * packageName  : com.nearsplit.domain.split_group.controller
 * fileName     : SplitGroupControllerTest
 * author       : jcy
 * date         : 2026-01-06(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-06(화)                jcy            최초 생성
 */

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Slf4j
class SplitGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthService authService;
    @Autowired
    private SplitGroupService splitGroupService;
    @Autowired
    private GroupParticipantRepository groupParticipantRepository;
    private Long userId;
    private String token;

    @BeforeEach
    void 설정() {
        RegisterRequest register = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("테스터1")
                .nickname("하늘다람쥐")
                .build();
        userId = authService.register(register);
        //authService.login(LoginRequest.builder().email("test1@test.com").password("test1234").build());
        token = jwtUtil.generateToken(userId);
    }

    @Test
    void 그룹_생성_성공() throws Exception {
        // given
        SplitGroupRequest request = new SplitGroupRequest();
        request.setTitle("사과 10kg 소분");
        request.setTotalPrice(BigDecimal.valueOf(50_000));
        request.setMaxParticipants(5);
        request.setPickupLocation("서울시 강남구");
        request.setClosedAt(LocalDate.now().plusDays(7));

        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/split")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // 그럼 이 시점에서 /api/split 호출 되면서 등록되고
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("사과 10kg 소분"))
                .andExpect(jsonPath("$.totalPrice").value(50000))
                .andExpect(jsonPath("$.maxParticipants").value(5));

    }
    @Test
    void 그룹_생성_실패_인증없음() throws Exception {
        // given
        SplitGroupRequest request = new SplitGroupRequest();
        request.setTitle("사과 10kg 소분");
        request.setTotalPrice(BigDecimal.valueOf(50_000));
        request.setMaxParticipants(5);
        request.setPickupLocation("서울시 강남구");
        request.setClosedAt(LocalDate.now().plusDays(7));

        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/split")
//                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 그룹_생성_실패_제목_누락() throws Exception {
        // given
        SplitGroupRequest request = new SplitGroupRequest();
        // request.setTitle(...);  // ← 제목 없음!
        request.setTotalPrice(new BigDecimal("50000"));
        request.setMaxParticipants(5);

        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/split")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());  // 400
    }

    @Test
    void 그룹_생성_실패_최대인원_1명이하() throws Exception {
        // given
        SplitGroupRequest request = new SplitGroupRequest();
        request.setTitle("사과 10kg 소분");
        request.setTotalPrice(new BigDecimal("50000"));
        request.setMaxParticipants(1);  // ← @Min(2) 위반!

        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/split")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());  // 400
    }


    @Test
    void 내_그룹_조회_방장() throws Exception {
        // given - 그룹 생성 (나는 방장)
        SplitGroupRequest request = new SplitGroupRequest();
        request.setTitle("사과 10kg 소분");
        request.setTotalPrice(new BigDecimal("50000"));
        request.setMaxParticipants(5);
        request.setPickupLocation("서울시 강남구");
        request.setClosedAt(LocalDate.now().plusDays(7));

        String requestBody = objectMapper.writeValueAsString(request);

        // 그룹 생성
        mockMvc.perform(post("/api/split")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // when & then - 내 그룹 조회
        mockMvc.perform(get("/api/split/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("사과 10kg 소분"))
                .andExpect(jsonPath("$[0].isHost").value(true));  // ← 방장 확인
    }

    @Test
    void 내_그룹_조회_빈_배열() throws Exception {
        // given
        // 그룹 생성하지 않음

        // when & then
        mockMvc.perform(get("/api/split/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

}