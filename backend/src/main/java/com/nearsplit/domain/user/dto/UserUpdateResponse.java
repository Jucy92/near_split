package com.nearsplit.domain.user.dto;

import com.nearsplit.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Builder
//@AllArgsConstructor
//@NoArgsConstructor
public class UserUpdateResponse {
    private String nickname;
    private String address;
    private String location;
    private String profileImage;
    private String phone;
    private LocalDateTime updatedAt;


    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .nickname(user.getNickname())
                .address(user.getAddress())
                .location(user.getLocation())
                .profileImage(user.getProfileImage())
                .phone(user.getPhone())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
