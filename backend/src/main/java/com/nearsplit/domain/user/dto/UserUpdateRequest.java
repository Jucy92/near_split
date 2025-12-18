package com.nearsplit.domain.user.dto;

import lombok.*;

@Getter /*@Setter*/ @Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserUpdateRequest {
    private String nickname;
    private String address;
    private String location;
    private String profileImage;
//    private String password;      // 사용자 정보 한번 더 받고 패스워드 변경하는 화면에서 얘만 따로 처리
    private String phone;

}
