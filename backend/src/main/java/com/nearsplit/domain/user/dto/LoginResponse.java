package com.nearsplit.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LoginResponse {
    //private String token; // 토큰은 쿠키에 담아서 보냄 / 직접 보내는 방식은 로컬스토리지에 저장 할 때
    //private Long id;
    //private String email;
    private UserResponse userResponse;

}
