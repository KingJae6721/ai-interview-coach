package com.aiinterview.user.service;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.user.dto.SignupRequest;
import com.aiinterview.user.dto.SignupResponse;
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
 *
 * <p>회원가입 처리 흐름:</p>
 * <ol>
 *     <li>이메일 중복 검사 ({@link UserRepository#existsByEmail})</li>
 *     <li>중복 시 {@link ErrorCode#DUPLICATE_EMAIL} 예외 발생</li>
 *     <li>비밀번호 BCrypt 암호화 ({@link PasswordEncoder#encode})</li>
 *     <li>{@link User} 엔티티 생성 (Builder 패턴)</li>
 *     <li>{@link UserRepository#save} 를 통한 DB 저장</li>
 *     <li>{@link SignupResponse} 반환</li>
 * </ol>
 *
 * <p>Controller는 이 Service만 호출하며,
 * Repository에는 비즈니스 로직이 존재하지 않는다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입을 처리한다.
     *
     * <p>신규 회원이므로 쓰기 트랜잭션(@Transactional)을 적용한다.
     * 이메일 중복 검사부터 DB 저장까지 하나의 트랜잭션으로 묶어
     * 데이터 정합성을 보장한다.</p>
     *
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 정보 DTO
     * @throws BusinessException DUPLICATE_EMAIL - 이미 가입된 이메일인 경우
     */
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Signup failed - duplicate email: {}", request.getEmail());
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 BCrypt 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성 (Builder 패턴 사용, Setter 사용 금지)
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
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
}
