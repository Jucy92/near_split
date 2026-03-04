package com.nearsplit.domain.user.dto;

import com.nearsplit.domain.user.entity.User;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Getter @Builder
//@AllArgsConstructor
//@NoArgsConstructor
public class UserUpdateResponse {
    private String nickname;
    private String address;
    private Double latitude;        // 위도 (Point에서 추출)
    private Double longitude;       // 경도 (Point에서 추출)
    private String profileImage;
    private String phone;
    private LocalDateTime updatedAt;


    public static UserUpdateResponse from(User user) {
        Point loc = user.getLocation();
        return UserUpdateResponse.builder()
                .nickname(user.getNickname())
                .address(user.getAddress())
                .latitude(loc != null ? loc.getY() : null)
                .longitude(loc != null ? loc.getX() : null)
                .profileImage(user.getProfileImage())
                .phone(user.getPhone())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
