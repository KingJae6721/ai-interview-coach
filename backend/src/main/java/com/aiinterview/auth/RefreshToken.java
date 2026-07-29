package com.aiinterview.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * Redis 보관용 RefreshToken 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private Long userId;

    @Indexed
    private String token;

    @TimeToLive
    private Long ttl; // 만료 시간 (초 단위)

    @Builder
    public RefreshToken(Long userId, String token, Long ttl) {
        this.userId = userId;
        this.token = token;
        this.ttl = ttl;
    }
}
