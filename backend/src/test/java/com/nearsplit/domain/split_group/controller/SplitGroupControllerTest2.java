package com.nearsplit.domain.split_group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.JwtUtil;
import com.nearsplit.domain.split_group.dto.ParticipantActionRequest;
import com.nearsplit.domain.split_group.dto.SplitGroupRequest;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
import com.nearsplit.domain.split_group.repository.SplitGroupRepository;
import com.nearsplit.domain.split_group.service.SplitGroupService;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class SplitGroupControllerTest2 {

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
    private SplitGroupRepository splitGroupRepository;
    @Autowired
    private ParticipantRepository groupParticipantRepository;
    private Long userId, userId2, userId3;
    private String token, token2, token3;

    @BeforeEach
    void 설정() {
        RegisterRequest register = RegisterRequest.builder()
                .email("test1@test.com").password("test1234").name("호스트").nickname("하늘다람쥐").build();
        userId = authService.register(register);
        RegisterRequest register2 = RegisterRequest.builder()
                .email("test2@test.com").password("test1234").name("테스터2").nickname("우이산토끼").build();
        userId2 = authService.register(register2);
        RegisterRequest register3 = RegisterRequest.builder()
                .email("test3@test.com").password("test1234").name("테스터3").nickname("빨래골빨래").build();
        userId3 = authService.register(register3);

        token = jwtUtil.generateToken(userId);
        token2 = jwtUtil.generateToken(userId2);
        token3 = jwtUtil.generateToken(userId3);

        SplitGroupRequest request = new SplitGroupRequest();
        request.setTitle("샤브샤브 재료 소분 모임");
        request.setTotalPrice(BigDecimal.valueOf(80_000));
        request.setMaxParticipants(4);
        request.setPickupLocation("강북구청");
        request.setClosedAt(LocalDate.now().plusDays(7));

        SplitGroup splitGroup = splitGroupService.createSplitGroup(userId, request);

//        splitGroupRepository.save(SplitGroup.builder().title("귤 3박스 나눔").totalPrice(BigDecimal.valueOf(1_000)).maxParticipants(10).hostUserId(userId).build());
        SplitGroupRequest request2 = new SplitGroupRequest();
        request2.setTitle("귤 3박스 나눔");
        request2.setTotalPrice(BigDecimal.valueOf(1_000));
        request2.setMaxParticipants(3);
        request2.setPickupLocation("수유3동우체국");
        request2.setClosedAt(LocalDate.now().plusDays(7));

        SplitGroup splitGroup2 = splitGroupService.createSplitGroup(userId, request2);

        SplitGroupRequest request3 = new SplitGroupRequest();
        request3.setTitle("사과 5kg 공동구매");
        request3.setTotalPrice(BigDecimal.valueOf(150_000));
        request3.setMaxParticipants(5);
        request3.setPickupLocation("우이천");
        request3.setClosedAt(LocalDate.now().plusDays(7));

        SplitGroup splitGroup3 = splitGroupService.createSplitGroup(userId2, request3);

        log.info("생성된 소그룹 정보={}",splitGroup);
    }


    @Test
    void 그룹_전체_조회_성공() throws Exception {
        mockMvc.perform(get("/api/split").header("Authorization", "Bearer " + token3).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECRUITING"));

    }
    @Test
    void 그룹_인원_가득_성공() throws Exception {
        // given
        mockMvc.perform(post("/api/split/1/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2));

        mockMvc.perform(post("/api/split/1/join")
                        .header("Authorization", "Bearer " + token3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3));
    }
}