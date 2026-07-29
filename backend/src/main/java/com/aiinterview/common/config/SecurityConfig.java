package com.aiinterview.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 기반 보안 설정.
 *
 * <p>
 * 현재 Sprint (User Domain):
 * </p>
 * <ul>
 * <li>CSRF 비활성화 (REST API는 쿠키 기반 인증을 사용하지 않으므로 불필요)</li>
 * <li>세션 STATELESS 설정 (JWT 기반 인증을 위한 사전 설정)</li>
 * <li>공개 경로 허용: /api/v1/auth/**, Swagger UI</li>
 * <li>나머지 경로는 현재 허용 (JWT 필터 추가 전 임시)</li>
 * <li>기본 폼 로그인 비활성화</li>
 * <li>기본 HTTP Basic 인증 비활성화</li>
 * </ul>
 *
 * <p>
 * 추후 Auth Sprint에서 아래 항목을 추가 구현한다:
 * </p>
 * <ul>
 * <li>JWT 기반 인증 필터 (JwtAuthenticationFilter)</li>
 * <li>보호 경로 인증 필수 설정 (.anyRequest().authenticated())</li>
 * <li>OAuth2 로그인 설정</li>
 * <li>CORS 설정</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        /**
         * 공개(인증 불필요) 경로 목록.
         *
         * <p>
         * Auth Sprint에서 JWT 필터가 추가될 때 이 목록을 기준으로
         * 인증 예외 경로를 관리한다.
         * </p>
         */
        private static final String[] PUBLIC_URLS = {
                        "/api/v1/auth/**", // 회원가입, 로그인 (인증 불필요)
                        "/v3/api-docs/**", // Swagger API 문서
                        "/swagger-ui/**", // Swagger UI
                        "/swagger-ui.html" // Swagger UI 진입점
        };

        /**
         * SecurityFilterChain을 등록한다.
         *
         * <p>
         * REST API 서버 기준 보안 설정:
         * </p>
         * <ul>
         * <li>CSRF 비활성화: REST API는 Stateless이므로 CSRF 공격 대상이 아님</li>
         * <li>세션 STATELESS: 서버 측 세션을 생성하지 않음 (JWT 사전 설정)</li>
         * <li>폼 로그인 비활성화: REST API이므로 HTML 로그인 페이지 불필요</li>
         * <li>HTTP Basic 비활성화: 브라우저 기본 인증 팝업 제거</li>
         * </ul>
         *
         * @param http HttpSecurity 설정 객체
         * @return 구성된 SecurityFilterChain
         * @throws Exception 설정 오류 시
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // CSRF 비활성화 (REST API는 쿠키/세션 기반 인증 미사용)
                                .csrf(AbstractHttpConfigurer::disable)

                                // 세션 Stateless 설정 (JWT 기반 인증 사전 준비)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 기본 폼 로그인 비활성화 (브라우저 로그인 페이지 제거)
                                .formLogin(AbstractHttpConfigurer::disable)

                                // HTTP Basic 인증 비활성화 (브라우저 인증 팝업 제거)
                                .httpBasic(AbstractHttpConfigurer::disable)

                                // 경로별 접근 권한 설정
                                .authorizeHttpRequests(auth -> auth
                                                // 공개 경로 — 인증 없이 접근 허용
                                                .requestMatchers(PUBLIC_URLS).permitAll()
                                                // 나머지 경로 — 현재 허용 (JWT 필터 추가 후 .authenticated()로 변경)
                                                .anyRequest().permitAll());

                return http.build();
        }

        /**
         * BCryptPasswordEncoder를 PasswordEncoder Bean으로 등록한다.
         *
         * <p>
         * BCrypt는 단방향 해시 함수이며, 내부적으로 salt를 자동 생성하여
         * 동일한 비밀번호라도 매번 다른 해시 값을 생성한다.
         * 이로 인해 Rainbow Table 공격에 강하며, Spring Security 표준 방식이다.
         * </p>
         *
         * @return BCryptPasswordEncoder 인스턴스
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
