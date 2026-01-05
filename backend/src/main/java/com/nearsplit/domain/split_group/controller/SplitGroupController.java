package com.nearsplit.domain.split_group.controller;

import com.nearsplit.domain.split_group.dto.SplitGroupRequest;
import com.nearsplit.domain.split_group.dto.SplitGroupResponse;
import com.nearsplit.domain.split_group.dto.SplitGroupSummaryResponse;
import com.nearsplit.domain.split_group.service.SplitGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/split")
@RequiredArgsConstructor
public class SplitGroupController {
    private final SplitGroupService splitGroupService;

    @GetMapping("/my")    // 나중에 사용자 ID나 타이틀로도 조회할 수 있어야하긴 하는데..
    public ResponseEntity<List<SplitGroupSummaryResponse>> getUserByAllGroup(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().body(splitGroupService.getMySplitGroups(userId));
    }

    @PostMapping
    public ResponseEntity<SplitGroupResponse> createGroup(@AuthenticationPrincipal Long userId, @RequestBody SplitGroupRequest splitGroupRequest) {
        return ResponseEntity.ok().body(splitGroupService.createSplitGroup(userId, splitGroupRequest));
    }
}
