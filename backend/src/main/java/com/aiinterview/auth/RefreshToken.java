package com.aiinterview.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Redis 보관용 RefreshToken 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    private Long userId;

    private String token;

    private Long ttl;

    @Builder
    public RefreshToken(Long userId, String token, Long ttl) {
        this.userId = userId;
        this.token = token;
        this.ttl = ttl;
    }
}
