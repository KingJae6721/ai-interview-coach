package com.aiinterview.auth;

import com.aiinterview.user.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration:1209600000}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Access Token을 생성한다.
     *
     * @param userId 회원 고유 ID (sub)
     * @param role 회원 권한 (role claim)
     * @return 생성된 JWT Access Token 문자열
     */
    public String createAccessToken(Long userId, UserRole role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token을 생성한다.
     *
     * @param userId 회원 고유 ID (sub)
     * @return 생성된 JWT Refresh Token 문자열 (14일 만료)
     */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpiration / 1000;
    }

    /**
     * 유효한 Access Token의 남은 만료 시간을 밀리초 단위로 반환한다.
     */
    public long getRemainingExpirationMillis(String token) {
        long remaining = getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    /**
     * 토큰의 유효성을 검증한다.
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 유효할 경우 true, 그렇지 않을 경우 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 Claims(페이로드)를 추출한다.
     *
     * @param token JWT 토큰 문자열
     * @return 추출된 Claims
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 사용자 고유 ID(Subject)를 추출한다.
     *
     * @param token JWT 토큰 문자열
     * @return 회원 고유 ID (Long)
     */
    public Long getUserId(String token) {
        String subject = getClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    /**
     * 토큰에서 사용자 권한(role claim)을 추출한다.
     *
     * @param token JWT 토큰 문자열
     * @return 권한명 문자열 (USER / ADMIN)
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }
}
