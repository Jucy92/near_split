package com.nearsplit.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class RegisterRequest {
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;
    @NotBlank(message = "필수 값 입니다.")
    private String password;
    @NotBlank(message = "필수 값 입니다.")
    private String name;
    @NotBlank(message = "필수 값 입니다.")
    private String nickname;

}
