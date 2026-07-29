package com.aiinterview.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 JWT 토큰 인가 성공 시 SecurityContextHolder에 저장")
    void doFilterInternal_Success() throws Exception {
        // given
        String token = "validAccessToken";
        given(request.getHeader("Authorization")).willReturn("Bearer " + token);
        given(request.getRequestURI()).willReturn("/api/v1/users/me");
        given(jwtProvider.validateToken(token)).willReturn(true);
        given(jwtProvider.getUserId(token)).willReturn(1L);
        given(userDetailsService.loadUserById(1L)).willReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰이 없거나 유효하지 않으면 예외 없이 다음 필터로 진행")
    void doFilterInternal_SkipAuthentication() throws Exception {
        // given
        given(request.getHeader("Authorization")).willReturn(null);
        given(request.getRequestURI()).willReturn("/api/v1/users/me");

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
