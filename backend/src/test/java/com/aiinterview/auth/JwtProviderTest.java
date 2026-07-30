package com.aiinterview.auth;

import com.aiinterview.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.aiinterview.auth.JwtProvider;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String secret = "c3ByaW5nLWJvb3QtYWktaW50ZXJ2aWV3LWNvYWNoLXBvcnRmb2xpby1zZWNyZXQta2V5LTMyei1ieXRlcy1sZW5ndGg=";
    private final long expiration = 1800000; // 30분 (1,800,000 ms)

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(secret, expiration, 1209600000L);
    }

    @Test
    @DisplayName("JWT 토큰 생성 및 정보 추출 검증")
    void createAndExtractToken() {
        // given
        Long userId = 123L;
        UserRole role = UserRole.USER;

        // when
        String token = jwtProvider.createAccessToken(userId, role);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtProvider.getRole(token)).isEqualTo(role.name());
    }

    @Test
    @DisplayName("잘못된 시그니처 또는 유효하지 않은 토큰 검증 실패")
    void validateToken_Fail_InvalidToken() {
        // given
        String malformedToken = "header.payload.signature";

        // when
        boolean result = jwtProvider.validateToken(malformedToken);

        // then
        assertThat(result).isFalse();
    }
}
