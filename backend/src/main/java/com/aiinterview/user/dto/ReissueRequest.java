package com.aiinterview.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ReissueRequest {

    @NotBlank(message = "Refresh Token은 필수 입력 항목입니다.")
    private final String refreshToken;

    @Builder
    private ReissueRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
