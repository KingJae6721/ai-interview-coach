package com.aiinterview.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 기반 보안 설정.
 *
 * <p>현재 Sprint (User Domain): PasswordEncoder Bean만 등록한다.</p>
 *
 * <p>추후 Sprint (Auth)에서 아래 항목을 추가 구현한다:</p>
 * <ul>
 *     <li>JWT 기반 인증 필터 (JwtAuthenticationFilter)</li>
 *     <li>SecurityFilterChain (공개/인증 필요 경로 분리)</li>
 *     <li>OAuth2 로그인 설정</li>
 *     <li>CORS 설정</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * BCryptPasswordEncoder를 PasswordEncoder Bean으로 등록한다.
     *
     * <p>BCrypt는 단방향 해시 함수이며, 내부적으로 salt를 자동 생성하여
     * 동일한 비밀번호라도 매번 다른 해시 값을 생성한다.
     * 이로 인해 Rainbow Table 공격에 강하며, Spring Security 표준 방식이다.</p>
     *
     * <p>{@link UserServiceImpl}에서 생성자 주입을 통해 사용된다.</p>
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
