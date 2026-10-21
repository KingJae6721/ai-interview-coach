package com.aiinterview.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Redis 기반 Refresh Token 저장소. */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    static final String KEY_PREFIX = "refreshToken:";
    private static final DefaultRedisScript<Long> SAVE_WITH_TTL_SCRIPT = new DefaultRedisScript<>("""
            redis.call('HSET', KEYS[1], 'userId', ARGV[1], 'token', ARGV[2], 'ttl', ARGV[3])
            return redis.call('PEXPIRE', KEYS[1], ARGV[3])
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RefreshToken save(RefreshToken refreshToken) {
        String key = key(refreshToken.getUserId());
        Long ttl = refreshToken.getTtl();
        if (ttl == null || ttl <= 0) {
            redisTemplate.delete(key);
            return refreshToken;
        }

        redisTemplate.execute(SAVE_WITH_TTL_SCRIPT, List.of(key),
                String.valueOf(refreshToken.getUserId()), refreshToken.getToken(), String.valueOf(ttl));
        return refreshToken;
    }

    public Optional<RefreshToken> findById(Long userId) {
        String key = key(userId);
        Object tokenValue = redisTemplate.opsForHash().get(key, "token");
        String token = tokenValue == null ? null : tokenValue.toString();
        if (token == null) {
            return Optional.empty();
        }

        Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        if (ttl == null || ttl <= 0) {
            redisTemplate.delete(key);
            return Optional.empty();
        }
        return Optional.of(RefreshToken.builder().userId(userId).token(token).ttl(ttl).build());
    }

    public void deleteById(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
