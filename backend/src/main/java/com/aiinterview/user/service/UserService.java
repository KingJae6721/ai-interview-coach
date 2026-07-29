package com.aiinterview.user.service;

import com.aiinterview.user.dto.LoginRequest;
import com.aiinterview.user.dto.LoginResponse;
import com.aiinterview.user.dto.SignupRequest;
import com.aiinterview.user.dto.SignupResponse;
import com.aiinterview.user.dto.UserResponse;

/**
 * 사용자 도메인 비즈니스 로직 인터페이스.
 *
 * <p>Service 계층의 계약(Contract)을 정의한다.
 * 구현체를 분리함으로써 테스트 시 Mock 주입이 용이하고,
 * 향후 확장(OAuth2, 소셜 로그인 등)에 유연하게 대응할 수 있다.</p>
 */
public interface UserService {

    /**
     * 회원가입을 처리한다.
     *
     * @param request 회원가입 요청 DTO (이메일, 비밀번호, 닉네임)
     * @return 회원가입 결과 DTO (id, 이메일, 닉네임)
     * @throws com.aiinterview.common.exception.BusinessException DUPLICATE_EMAIL - 이메일 중복 시
     */
    SignupResponse signup(SignupRequest request);

    /**
     * 로그인을 처리한다.
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return 로그인 결과 DTO (id, 이메일, 닉네임, role)
     * @throws com.aiinterview.common.exception.BusinessException USER_NOT_FOUND - 회원이 존재하지 않는 경우
     * @throws com.aiinterview.common.exception.BusinessException INVALID_PASSWORD - 비밀번호가 틀린 경우
     */
    LoginResponse login(LoginRequest request);

    /**
     * 현재 로그인한 사용자의 정보를 조회한다.
     *
     * @param userId 회원 고유 ID
     * @return 사용자 정보 DTO (id, email, nickname, role)
     * @throws com.aiinterview.common.exception.BusinessException USER_NOT_FOUND - 회원이 존재하지 않거나 탈퇴한 경우
     */
    UserResponse getMyInfo(Long userId);
}


