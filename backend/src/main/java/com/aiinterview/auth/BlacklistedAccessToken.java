package com.aiinterview.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Redis에 저장하는 로그아웃된 Access Token 정보.
 *
 * <p>토큰 자체를 키로 사용하며, TTL이 만료되면 Redis가 자동으로 삭제한다.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedAccessToken {

    private String token;

    private Long ttl;

    @Builder
    public BlacklistedAccessToken(String token, Long ttl) {
        this.token = token;
        this.ttl = ttl;
    }
}
