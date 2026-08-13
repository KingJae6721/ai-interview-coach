package com.aiinterview.user.service;

import com.aiinterview.auth.JwtProvider;
import com.aiinterview.auth.BlacklistedAccessToken;
import com.aiinterview.auth.BlacklistedAccessTokenRepository;
import com.aiinterview.auth.RefreshToken;
import com.aiinterview.auth.RefreshTokenRepository;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.user.dto.LoginRequest;
import com.aiinterview.user.dto.LoginResponse;
import com.aiinterview.user.dto.ReissueRequest;
import com.aiinterview.user.dto.ReissueResponse;
import com.aiinterview.user.dto.SignupRequest;
import com.aiinterview.user.dto.SignupResponse;
import com.aiinterview.user.dto.UserResponse;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 도메인 비즈니스 로직 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    /**
     * 회원가입을 처리한다.
     *
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 정보 DTO
     * @throws BusinessException DUPLICATE_EMAIL - 이미 가입된 이메일인 경우
     */
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            log.error("Signup failed - duplicate email: {}", request.email());
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 BCrypt 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. User 엔티티 생성 (Builder 패턴 사용, Setter 사용 금지)
        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .build();

        // 4. DB 저장
        User savedUser = userRepository.save(user);

        log.info("Signup completed - userId: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        // 5. Response DTO 반환 (Entity를 직접 반환하지 않는다)
        return SignupResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .build();
    }

    /**
     * 로그인을 처리한다.
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 완료된 사용자 정보, AccessToken 및 RefreshToken DTO
     * @throws BusinessException USER_NOT_FOUND - 회원이 존재하지 않는 경우
     * @throws BusinessException INVALID_PASSWORD - 비밀번호가 일치하지 않는 경우
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // 1. 이메일로 회원 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Login failed - user not found: {}", request.email());
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 2. Soft Delete(탈퇴) 회원 체크
        if (user.getStatus() == UserStatus.DELETED) {
            log.error("Login failed - deleted user account: {}", request.email());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 비밀번호 일치 확인 (matches() 사용)
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.error("Login failed - invalid password for: {}", request.email());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 4. JWT AccessToken & RefreshToken 생성
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // 5. RefreshToken Redis 저장
        RefreshToken redisToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .ttl(jwtProvider.getRefreshExpirationSeconds())
                .build();
        refreshTokenRepository.save(redisToken);

        log.info("Login completed - userId: {}, email: {}", user.getId(), user.getEmail());

        // 6. Response DTO 반환
        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 현재 로그인한 사용자의 정보를 조회한다.
     *
     * @param userId 회원 고유 ID
     * @return 사용자 정보 DTO
     * @throws BusinessException USER_NOT_FOUND - 회원이 존재하지 않거나 탈퇴한 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {

        // 1. 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("GetMyInfo failed - user not found: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 2. Soft Delete(탈퇴) 회원 체크
        if (user.getStatus() == UserStatus.DELETED) {
            log.error("GetMyInfo failed - deleted user account: {}", requestUserId(userId));
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. UserResponse 반환 (비밀번호 등 민감정보 제외)
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    private String requestUserId(Long userId) {
        return String.valueOf(userId);
    }

    /**
     * Refresh Token을 검증하고 새 Access Token을 재발급한다.
     *
     * <p>처리 흐름:</p>
     * <ol>
     *     <li>JwtProvider로 토큰 서명/만료 검증</li>
     *     <li>Redis에서 저장된 RefreshToken 조회</li>
     *     <li>수신된 토큰과 Redis 토큰 일치 여부 확인</li>
     *     <li>userId로 회원 조회 후 새 AccessToken 생성</li>
     * </ol>
     *
     * @param request ReissueRequest (refreshToken)
     * @return 재발급된 AccessToken DTO
     * @throws BusinessException INVALID_TOKEN - 토큰이 유효하지 않거나 Redis에 없는 경우
     */
    @Override
    @Transactional
    public ReissueResponse reissue(ReissueRequest request) {

        String refreshToken = request.refreshToken();

        // 1. Refresh Token 서명/만료 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            log.error("Reissue failed - invalid refresh token");
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2. userId 추출
        Long userId = jwtProvider.getUserId(refreshToken);

        // 3. Redis에서 저장된 RefreshToken 조회
        RefreshToken storedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Reissue failed - refresh token not found in Redis, userId: {}", userId);
                    return new BusinessException(ErrorCode.INVALID_TOKEN);
                });

        // 4. 수신된 토큰과 Redis 토큰 일치 확인
        if (!storedToken.getToken().equals(refreshToken)) {
            log.error("Reissue failed - refresh token mismatch, userId: {}", userId);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 5. 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Reissue failed - user not found: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        // 6. 새 AccessToken 발급
        refreshTokenRepository.deleteById(userId);

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .token(newRefreshToken)
                .ttl(jwtProvider.getRefreshExpirationSeconds())
                .build());

        log.info("Reissue completed - userId: {}", userId);

        return ReissueResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {
        String accessToken = extractBearerToken(authorizationHeader);

        if (!jwtProvider.validateToken(accessToken)
                || blacklistedAccessTokenRepository.existsById(accessToken)) {
            log.error("Logout failed - invalid access token");
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(accessToken);
        long remainingExpirationMillis = jwtProvider.getRemainingExpirationMillis(accessToken);

        if (remainingExpirationMillis <= 0) {
            log.error("Logout failed - expired access token, userId: {}", userId);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        refreshTokenRepository.deleteById(userId);
        blacklistedAccessTokenRepository.save(BlacklistedAccessToken.builder()
                .token(accessToken)
                .ttl(remainingExpirationMillis)
                .build());

        log.info("Logout completed - userId: {}", userId);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String accessToken = authorizationHeader.substring("Bearer ".length());
        if (accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return accessToken;
    }
}
