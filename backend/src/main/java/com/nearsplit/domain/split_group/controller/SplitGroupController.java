package com.nearsplit.domain.split_group.controller;

import com.nearsplit.domain.split_group.dto.*;
import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.entity.SplitGroup;
import com.nearsplit.domain.split_group.service.SplitGroupService;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/split")
@RequiredArgsConstructor
public class SplitGroupController {
    private final SplitGroupService splitGroupService;

    @GetMapping
    public ResponseEntity<Page<SplitGroupResponse>> getAllGroup() {
        Sort sort = Sort.by(Sort.Direction.DESC, "CreatedAt");
        //Sort sort = Sort.by(Sort.Direction.valueOf(sortDirect), sortBy);  // 단일 정렬
        //Sort sort = Sort.by(Sort.Direction.valueOf(sortDirect), sortBy) // 다중 정렬
        //        .and(Sort.by(Sort.Direction.ASC, "id"));
        PageRequest pageRequest = PageRequest.of(0, 10, sort);

        Page<SplitGroup> splitGroupsPage = splitGroupService.getAllRecruitingGroups(pageRequest);
        Page<SplitGroupResponse> responsePage = splitGroupsPage.map(SplitGroupResponse::from);      // Entity -> DTO 변경 작업
        
        return ResponseEntity.ok().body(responsePage);
    }

    @PostMapping
    public ResponseEntity<SplitGroupResponse> createGroup(@AuthenticationPrincipal Long userId, @Validated @RequestBody SplitGroupRequest splitGroupRequest) {
        SplitGroup splitGroup = splitGroupService.createSplitGroup(userId, splitGroupRequest);
        return ResponseEntity.ok().body(SplitGroupResponse.from(splitGroup));
    }

    @GetMapping("/my")    // 나중에 사용자 ID나 타이틀로도 조회할 수 있어야하긴 하는데..
    public ResponseEntity<List<SplitGroupSummaryResponse>> getUserByAllGroup(@AuthenticationPrincipal Long userId) {

        List<Participant> participantList = splitGroupService.getMySplitGroups(userId);

        List<SplitGroupSummaryResponse> responses = new ArrayList<>();  // 본인이 관리장이 아닌 디테일->마스터 기본 정보(번호,제목,인원,금액 등)

        for (Participant participant : participantList) {
            responses.add(SplitGroupSummaryResponse.from(participant));
        }

        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SplitGroupResponse> getSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        SplitGroup splitGroup = splitGroupService.getSplitGroup(groupId, userId);
        return ResponseEntity.ok().body(SplitGroupResponse.from(splitGroup));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<?> updateSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long userId
                                            , @RequestBody SplitGroupRequest groupRequest) {
        return ResponseEntity.ok().body(splitGroupService.updateSplitGroup(groupId, userId, groupRequest));
    }
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().body(splitGroupService.deleteSplitGroup(groupId, userId));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        Participant participant = splitGroupService.joinSplitGroup(groupId, userId);
        return ResponseEntity.ok().body(participant);
    }

    @DeleteMapping("/{groupId}/join")
    public ResponseEntity<?> cancelJoin(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().body(splitGroupService.cancelJoin(groupId, userId)); // 삭제된 내용 전달
    }

    @PostMapping("/{groupId}/approve")
    public ResponseEntity<ParticipantResponse> approveSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long hostId,
                                                                 @Validated @RequestBody ParticipantActionRequest participantActionRequest) {

        Participant updated = splitGroupService.approveParticipant(groupId, hostId, participantActionRequest);
        return ResponseEntity.ok(ParticipantResponse.from(updated));  // 204 리턴
    }

    @PostMapping("/{groupId}/reject")
    public ResponseEntity<ParticipantResponse> rejectSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long hostId,
                                                                @Validated @RequestBody ParticipantActionRequest participantActionRequest) {
        Participant updated = splitGroupService.rejectedParticipant(groupId, hostId, participantActionRequest);
        return ResponseEntity.ok(ParticipantResponse.from(updated));
    }

    @GetMapping("/{groupId}/participants")
    public ResponseEntity<?> getParticipantCount(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        long participantCount = splitGroupService.getParticipantCount(groupId, userId);
        return ResponseEntity.ok().body(participantCount);
    }

}
