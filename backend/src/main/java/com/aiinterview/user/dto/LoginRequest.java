package com.aiinterview.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        String password
) {

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
