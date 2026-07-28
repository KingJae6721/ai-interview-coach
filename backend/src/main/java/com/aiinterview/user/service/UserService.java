package com.aiinterview.user.service;

import com.aiinterview.user.dto.SignupRequest;
import com.aiinterview.user.dto.SignupResponse;

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
}
