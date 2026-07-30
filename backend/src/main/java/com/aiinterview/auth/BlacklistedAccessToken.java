package com.aiinterview.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

/**
 * Redis에 저장하는 로그아웃된 Access Token 정보.
 *
 * <p>토큰 자체를 키로 사용하며, TTL이 만료되면 Redis가 자동으로 삭제한다.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "accessTokenBlacklist")
public class BlacklistedAccessToken {

    @Id
    private String token;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long ttl;

    @Builder
    public BlacklistedAccessToken(String token, Long ttl) {
        this.token = token;
        this.ttl = ttl;
    }
}
