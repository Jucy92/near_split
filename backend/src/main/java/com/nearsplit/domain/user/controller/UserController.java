package com.nearsplit.domain.user.controller;

import com.nearsplit.domain.user.dto.UserResponse;
import com.nearsplit.domain.user.dto.UserUpdateRequest;
import com.nearsplit.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().body(userService.getProfile(userId));

    }
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal Long userid, @RequestBody UserUpdateRequest updateRequest) {

        UserResponse response = userService.updateProfile(userid, updateRequest);

        return ResponseEntity.ok().body(response);
    }
}
