package com.nearsplit.domain.split_group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.security.JwtUtil;
import com.nearsplit.domain.split_group.dto.ParticipantActionRequest;
import com.nearsplit.domain.split_group.dto.SplitGroupRequest;
import com.nearsplit.domain.split_group.entity.ParticipantStatus;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.repository.ParticipantRepository;
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
    private ParticipantRepository groupParticipantRepository;
    private Long userId, userId2, userId3;
    private Long splitGroupId;  // BeforeEach에서 생성된 그룹 ID
    private String token, token2, token3;

    @BeforeEach
    void 설정() {
        RegisterRequest register = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("호스트")
                .nickname("하늘다람쥐")
                .build();
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
        splitGroupId = splitGroup.getId();  // 생성된 그룹 ID 저장
        log.info("생성된 소그룹 정보={}",splitGroup);
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
    void 내_그룹_조회_참여자() throws Exception {
        // given - user2가 기존 그룹(BeforeEach에서 생성)에 참여 신청 후 승인
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON));

        ParticipantActionRequest approveRequest = new ParticipantActionRequest();
        approveRequest.setParticipantUserId(userId2);
        String approveBody = objectMapper.writeValueAsString(approveRequest);

        mockMvc.perform(post("/api/split/" + splitGroupId + "/approve")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(approveBody));

        // when & then - user2의 내 그룹 조회
        mockMvc.perform(get("/api/split/my")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("샤브샤브 재료 소분 모임"))
                .andExpect(jsonPath("$[0].isHost").value(false));  // ← 참여자 확인
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

    @Test
    void 그룹_참여_신청_성공() throws Exception {
        // joinSplitGroup은 ApiResponse로 감싸서 반환 → $.data.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId2.intValue()));

        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId3.intValue()));
    }
    @Test
    void 그룹_참여_신청_실패_인증없음() throws Exception {
        // 인증 없이 요청 시 401 Unauthorized
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        //.header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void 그룹_참여_신청_실패_중복요청() throws Exception {
        // joinSplitGroup은 ApiResponse로 감싸서 반환 → $.data.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId2.intValue()));

        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 참여 신청한 그룹입니다."));
    }
    @Test
    void 그룹_참여_신청_실패_존재하지않는그룹() throws Exception {
        // BusinessException(GROUP_NOT_FOUND) → 404 NOT_FOUND
        mockMvc.perform(post("/api/split/999/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 그룹입니다"));

    }

    @Test
    void 참여자_승인_성공() throws Exception {
        // given - joinSplitGroup은 ApiResponse로 감싸서 반환 → $.data.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId2.intValue()))
                .andExpect(jsonPath("$.data.status").value(ParticipantStatus.PENDING.name()));

        ParticipantActionRequest request = new ParticipantActionRequest();
        request.setParticipantUserId(userId2);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then - approveSplitGroup은 ParticipantResponse 직접 반환 → $.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/approve")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId2.intValue()))
                .andExpect(jsonPath("$.status").value(ParticipantStatus.APPROVED.name()));
    }
    @Test
    void 참여자_승인_실패_방장이아님() throws Exception {
        // given - joinSplitGroup은 ApiResponse로 감싸서 반환 → $.data.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId2.intValue()))
                .andExpect(jsonPath("$.data.status").value(ParticipantStatus.PENDING.name()));

        ParticipantActionRequest request = new ParticipantActionRequest();
        request.setParticipantUserId(userId2);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/split/" + splitGroupId + "/approve")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("방장만 승인할 수 있습니다."));
    }
    @Test
    void 참여자_거절_성공() throws Exception {
        // given - joinSplitGroup은 ApiResponse로 감싸서 반환 → $.data.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId2.intValue()))
                .andExpect(jsonPath("$.data.status").value(ParticipantStatus.PENDING.name()));

        ParticipantActionRequest request = new ParticipantActionRequest();
        request.setParticipantUserId(userId2);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then - rejectSplitGroup은 ParticipantResponse 직접 반환 → $.userId
        // 거절 시 참여자 삭제됨 (REJECTED 상태로 남기지 않음)
        // 반환되는 participant는 삭제 전 객체이므로 userId 확인만 가능
        mockMvc.perform(post("/api/split/" + splitGroupId + "/reject")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId2.intValue()));
                // 참여자가 삭제되었으므로 status 체크 제거
    }
    @Test
    void 참여자_거절_실패_방장이아님() throws Exception {
        // given - joinSplitGroup은 ApiResponse로 감싸서 반환 → $.data.userId
        mockMvc.perform(post("/api/split/" + splitGroupId + "/join")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId2.intValue()))
                .andExpect(jsonPath("$.data.status").value(ParticipantStatus.PENDING.name()));

        ParticipantActionRequest request = new ParticipantActionRequest();
        request.setParticipantUserId(userId2);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/split/" + splitGroupId + "/reject")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("방장만 거절할 수 있습니다."));
    }

    @Test
    void 그룹_전체_조회_성공() {

    }
}