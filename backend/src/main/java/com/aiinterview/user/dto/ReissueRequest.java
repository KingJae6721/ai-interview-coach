package com.aiinterview.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ReissueRequest(
        @NotBlank(message = "Refresh Token은 필수 입력 값입니다.")
        String refreshToken
) {

    public String getRefreshToken() {
        return refreshToken;
    }
}
