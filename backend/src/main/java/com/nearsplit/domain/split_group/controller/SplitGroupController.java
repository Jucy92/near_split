package com.nearsplit.domain.split_group.controller;

import com.nearsplit.domain.split_group.dto.*;
import com.nearsplit.domain.split_group.entity.Participant;
import com.nearsplit.domain.split_group.service.SplitGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/split")
@RequiredArgsConstructor
public class SplitGroupController {
    private final SplitGroupService splitGroupService;

    @PostMapping
    public ResponseEntity<SplitGroupResponse> createGroup(@AuthenticationPrincipal Long userId, @Validated @RequestBody SplitGroupRequest splitGroupRequest) {
        return ResponseEntity.ok().body(splitGroupService.createSplitGroup(userId, splitGroupRequest));
    }

    @GetMapping("/my")    // 나중에 사용자 ID나 타이틀로도 조회할 수 있어야하긴 하는데..
    public ResponseEntity<List<SplitGroupSummaryResponse>> getUserByAllGroup(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().body(splitGroupService.getMySplitGroups(userId));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SplitGroupResponse> getSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().body(splitGroupService.getSplitGroup(groupId, userId));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
        splitGroupService.joinSplitGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{groupId}/approve")
    public ResponseEntity<ParticipantResponse> approveSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long hostId,
                                                                 @Validated ParticipantActionRequest participantActionRequest) {
        Participant updated = splitGroupService.approveParticipant(groupId, hostId, participantActionRequest);
        return ResponseEntity.ok(ParticipantResponse.from(updated));  // 204 리턴
    }
    @PostMapping("/{groupId}/reject")
    public ResponseEntity<ParticipantResponse> rejectSplitGroup(@PathVariable Long groupId, @AuthenticationPrincipal Long hostId,
                                            @Validated ParticipantActionRequest participantActionRequest) {
        Participant updated = splitGroupService.approveParticipant(groupId, hostId, participantActionRequest);
        return ResponseEntity.ok(ParticipantResponse.from(updated));
    }

}
