package com.nearsplit.domain.user.controller;

import com.nearsplit.domain.user.dto.UserUpdateRequest;
import com.nearsplit.domain.user.dto.UserUpdateResponse;
import com.nearsplit.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/me")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal long userid, UserUpdateRequest updateRequest) {

        UserUpdateResponse response = userService.updateProfile(userid, updateRequest);

        return ResponseEntity.ok().body(response);
    }
}
