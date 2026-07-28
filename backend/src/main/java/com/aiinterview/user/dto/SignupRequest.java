package com.aiinterview.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SignupRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Size(max = 255, message = "이메일은 최대 255자까지 입력할 수 있습니다.")
    private final String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,20}$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 최소 1개씩 포함해야 합니다."
    )
    private final String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해야 합니다.")
    private final String nickname;

    @Builder
    private SignupRequest(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
}
