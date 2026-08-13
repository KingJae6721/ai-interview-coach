package com.aiinterview.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignupRequest(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 최대 255자까지 입력할 수 있습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
        @Pattern(
                regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,20}$",
                message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 최소 1개씩 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "닉네임은 필수 입력 값입니다.")
        @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해야 합니다.")
        String nickname
) {

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }
}
