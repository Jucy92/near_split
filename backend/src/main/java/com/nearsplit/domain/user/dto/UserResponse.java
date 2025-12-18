package com.nearsplit.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nearsplit.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 제외하고 JSON 변환 -> 네트워크 비용 걱정으로 DTO 를  나눴는데, 이 어노테이션으로 해결하고 DTO 줄일 수 있음
// ㄴ> 전역 설정은 yml 파일 (주석되어있음)
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String address;
    private String location;
    private String profileImage;
    private String phone;
    private LocalDateTime updatedAt;

    // 로그인용 - 기본 정보만
    public static UserResponse fromBasic(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .build();
    }

    // 프로필 조회용 - 전체 정보
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .address(user.getAddress())
                .location(user.getLocation())
                .profileImage(user.getProfileImage())
                .phone(user.getPhone())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
