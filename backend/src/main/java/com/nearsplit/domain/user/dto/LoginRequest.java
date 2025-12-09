package com.nearsplit.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder
public class LoginRequest {

    @Email @NotNull(message = "이메일 주소를 입력해 주세요")
    private String email;
    @NotNull(message = "비밀번호를 입력해 주세요")
    private String password;
}
