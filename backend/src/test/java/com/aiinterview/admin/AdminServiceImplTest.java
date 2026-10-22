package com.aiinterview.admin;

import com.aiinterview.admin.repository.AdminAuditLogRepository;
import com.aiinterview.admin.service.AdminServiceImpl;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposting.repository.JobPostingAnalysisRepository;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;
    @Mock
    private JobPostingAnalysisRepository jobPostingAnalysisRepository;
    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(
                userRepository,
                interviewRepository,
                interviewQuestionRepository,
                jobPostingAnalysisRepository,
                adminAuditLogRepository);
    }

    @Test
    void roleChangeCreatesAuditLogWithActorAndBeforeAfterValues() {
        User target = user(UserRole.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.updateUserRole(1L, 2L, UserRole.ADMIN);

        assertThat(target.getRole()).isEqualTo(UserRole.ADMIN);
        ArgumentCaptor<com.aiinterview.admin.entity.AdminAuditLog> captor =
                ArgumentCaptor.forClass(com.aiinterview.admin.entity.AdminAuditLog.class);
        verify(adminAuditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActorUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getTargetId()).isEqualTo(2L);
        assertThat(captor.getValue().getBeforeValue()).isEqualTo("USER");
        assertThat(captor.getValue().getAfterValue()).isEqualTo("ADMIN");
    }

    @Test
    void lastAdminDemotionIsRejectedWithoutAuditLog() {
        User target = user(UserRole.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findAllByRoleForUpdate(UserRole.ADMIN)).thenReturn(List.of(target));

        assertThatThrownBy(() -> adminService.updateUserRole(2L, 2L, UserRole.USER))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.LAST_ADMIN_DEMOTION_NOT_ALLOWED));

        assertThat(target.getRole()).isEqualTo(UserRole.ADMIN);
        verify(adminAuditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sameRoleRequestIsIdempotentWithoutAuditLog() {
        User target = user(UserRole.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminService.updateUserRole(1L, 2L, UserRole.USER);

        verify(userRepository, never()).findAllByRoleForUpdate(UserRole.ADMIN);
        verify(adminAuditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private User user(UserRole role) {
        return User.builder()
                .email(role.name().toLowerCase() + "@example.com")
                .password("encoded-password")
                .nickname(role.name())
                .role(role)
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
