package com.aiinterview.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Redis 기반 Access Token 블랙리스트 저장소. */
@Repository
@RequiredArgsConstructor
public class BlacklistedAccessTokenRepository {

    static final String KEY_PREFIX = "accessTokenBlacklist:";
    private static final DefaultRedisScript<Long> SAVE_WITH_TTL_SCRIPT = new DefaultRedisScript<>("""
            redis.call('HSET', KEYS[1], 'token', ARGV[1], 'ttl', ARGV[2])
            return redis.call('PEXPIRE', KEYS[1], ARGV[2])
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public BlacklistedAccessToken save(BlacklistedAccessToken blacklistedAccessToken) {
        Long ttl = blacklistedAccessToken.getTtl();
        if (ttl == null || ttl <= 0) {
            return blacklistedAccessToken;
        }

        String key = key(blacklistedAccessToken.getToken());
        redisTemplate.execute(SAVE_WITH_TTL_SCRIPT, List.of(key),
                blacklistedAccessToken.getToken(), String.valueOf(ttl));
        return blacklistedAccessToken;
    }

    public boolean existsById(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }

    private String key(String token) {
        return KEY_PREFIX + token;
    }
}
