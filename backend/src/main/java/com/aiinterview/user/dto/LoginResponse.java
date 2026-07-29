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

    @Builder
    private LoginResponse(Long id, String email, String nickname, UserRole role) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
}
