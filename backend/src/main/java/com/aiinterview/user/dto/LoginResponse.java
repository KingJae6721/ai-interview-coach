package com.aiinterview.user.dto;

import com.aiinterview.user.entity.UserRole;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class LoginResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final UserRole role;
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;

    @Builder
    private LoginResponse(Long id, String email, String nickname, UserRole role, String accessToken, String refreshToken, String tokenType) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
    }
}
