package com.aiinterview.user.service;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.user.dto.SignupRequest;
import com.aiinterview.user.dto.SignupResponse;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("TC-01: 회원가입 성공")
    void signup_Success() {
        // given
        SignupRequest request = SignupRequest.builder()
                .email("test@test.com")
                .password("1234Abcd!")
                .nickname("홍길동")
                .build();

        User savedUser = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .nickname(request.getNickname())
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 1L); // DB에서 자동 생성된 ID 모사

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        SignupResponse response = userService.signup(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getNickname()).isEqualTo("홍길동");

        // verify (호출 횟수 검증)
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC-02: 중복 이메일 가입 실패")
    void signup_Fail_DuplicateEmail() {
        // given
        SignupRequest request = SignupRequest.builder()
                .email("test@test.com")
                .password("1234Abcd!")
                .nickname("홍길동")
                .build();

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.signup(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        // verify (중복 시 암호화 및 저장이 절대 수행되지 않아야 함)
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("TC-03: 비밀번호 암호화 저장")
    void signup_PasswordEncoding() {
        // given
        SignupRequest request = SignupRequest.builder()
                .email("test@test.com")
                .password("1234Abcd!")
                .nickname("홍길동")
                .build();

        User savedUser = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .nickname(request.getNickname())
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // 저장하려는 엔티티 내부 암호가 암호화된 값인지 단언(Assert)
            assertThat(userToSave.getPassword()).isEqualTo("encodedPassword");
            return savedUser;
        });

        // when
        userService.signup(request);

        // then & verify
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
