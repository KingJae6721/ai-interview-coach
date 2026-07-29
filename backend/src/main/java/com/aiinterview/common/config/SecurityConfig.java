package com.aiinterview.common.config;

import com.aiinterview.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 기반 보안 설정.
 *
 * <p>REST API 기반의 Stateless 보안 환경을 구성하고
 * JwtAuthenticationFilter를 필터 체인에 등록합니다.</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 공개(인증 불필요) 경로 목록.
     */
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",  // 회원가입, 로그인 등 인증 API
            "/swagger-ui/**",   // Swagger UI static resources
            "/swagger-ui.html", // Swagger UI HTML 진입점
            "/v3/api-docs/**",  // OpenAPI 문서 규격
            "/h2-console/**"    // H2 콘솔 (개발용)
    };

    /**
     * SecurityFilterChain을 등록한다.
     *
     * @param http HttpSecurity 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API는 Stateless이므로 CSRF 미사용)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 STATELESS 설정 (서버 측 세션 미생성)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated())

                // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 이전에 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder를 PasswordEncoder Bean으로 등록한다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
