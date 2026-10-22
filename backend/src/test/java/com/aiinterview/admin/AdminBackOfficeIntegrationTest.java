package com.aiinterview.admin;

import com.aiinterview.admin.entity.AdminAuditAction;
import com.aiinterview.admin.repository.AdminAuditLogRepository;
import com.aiinterview.auth.BlacklistedAccessTokenRepository;
import com.aiinterview.auth.JwtProvider;
import com.aiinterview.evaluation.entity.QuestionEvaluation;
import com.aiinterview.evaluation.repository.QuestionEvaluationRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import com.aiinterview.interview.entity.InterviewQuestionType;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class AdminBackOfficeIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ai_interview")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("jwt.secret", () ->
                "c3ByaW5nLWJvb3QtYWktaW50ZXJ2aWV3LWNvYWNoLXRlc3Qtc2VjcmV0LWtleS0zMng=");
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private InterviewQuestionRepository interviewQuestionRepository;

    @Autowired
    private InterviewAnswerRepository interviewAnswerRepository;

    @Autowired
    private QuestionEvaluationRepository questionEvaluationRepository;

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        when(blacklistedAccessTokenRepository.existsById(anyString())).thenReturn(false);

        questionEvaluationRepository.deleteAll();
        interviewAnswerRepository.deleteAll();
        interviewQuestionRepository.deleteAll();
        interviewRepository.deleteAll();
        adminAuditLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void adminApiRequiresAdminRole() throws Exception {
        User normalUser = saveUser("user@example.com", UserRole.USER);
        User admin = saveUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(normalUser)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void adminCanFilterUsersWithoutNPlusOneDataLoading() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);
        User matchedUser = saveUser("member.one@example.com", UserRole.USER);
        saveUser("member.two@example.com", UserRole.USER);
        interviewRepository.save(Interview.builder()
                .user(matchedUser)
                .title("첫 면접")
                .status(InterviewStatus.READY)
                .build());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(admin))
                        .param("email", "member.one")
                        .param("role", "USER")
                        .param("status", "ACTIVE")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value(matchedUser.getId()))
                .andExpect(jsonPath("$.data.content[0].interviewCount").value(1));
    }

    @Test
    void roleChangeAndAuditLogAreCommittedTogether() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);
        User target = saveUser("target@example.com", UserRole.USER);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        assertThat(userRepository.findById(target.getId()).orElseThrow().getRole())
                .isEqualTo(UserRole.ADMIN);
        assertThat(adminAuditLogRepository.findAll())
                .singleElement()
                .satisfies(auditLog -> {
                    assertThat(auditLog.getActorUserId()).isEqualTo(admin.getId());
                    assertThat(auditLog.getTargetId()).isEqualTo(target.getId());
                    assertThat(auditLog.getAction()).isEqualTo(AdminAuditAction.USER_ROLE_CHANGED);
                    assertThat(auditLog.getBeforeValue()).isEqualTo("USER");
                    assertThat(auditLog.getAfterValue()).isEqualTo("ADMIN");
                });

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", bearer(admin))
                        .param("action", "USER_ROLE_CHANGED")
                        .param("actorUserId", admin.getId().toString())
                        .param("targetType", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].targetId").value(target.getId()));
    }

    @Test
    void sameRoleIsIdempotentAndDoesNotCreateAuditLog() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);
        User target = saveUser("target@example.com", UserRole.USER);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("USER"));

        assertThat(adminAuditLogRepository.count()).isZero();
    }

    @Test
    void lastAdminCannotBeDemotedAndFailedChangeLeavesNoAuditLog() throws Exception {
        User onlyAdmin = saveUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", onlyAdmin.getId())
                        .header("Authorization", bearer(onlyAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_ADMIN_DEMOTION_NOT_ALLOWED"));

        assertThat(userRepository.findById(onlyAdmin.getId()).orElseThrow().getRole())
                .isEqualTo(UserRole.ADMIN);
        assertThat(adminAuditLogRepository.count()).isZero();
    }

    @Test
    void missingUserRoleChangeReturnsNotFound() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", 999999L)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

        assertThat(adminAuditLogRepository.count()).isZero();
    }

    @Test
    void invalidRoleValueReturnsBadRequest() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);
        User target = saveUser("target@example.com", UserRole.USER);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", target.getId())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"OWNER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));

        assertThat(adminAuditLogRepository.count()).isZero();
    }

    @Test
    void adminCanFilterAndInspectAnyUsersInterview() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);
        User owner = saveUser("owner@example.com", UserRole.USER);
        Interview interview = interviewRepository.save(Interview.builder()
                .user(owner)
                .title("운영 확인 면접")
                .status(InterviewStatus.READY)
                .build());
        InterviewQuestion question = interviewQuestionRepository.save(InterviewQuestion.builder()
                .interview(interview)
                .questionOrder(1)
                .content("트랜잭션을 설명해 주세요.")
                .category(InterviewQuestionCategory.CS)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .type(InterviewQuestionType.NORMAL)
                .isAiGenerated(true)
                .build());
        InterviewAnswer answer = interviewAnswerRepository.save(InterviewAnswer.builder()
                .interviewQuestion(question)
                .answerContent("원자성, 일관성, 격리성, 지속성을 보장합니다.")
                .answeredAt(LocalDateTime.now())
                .build());
        questionEvaluationRepository.save(QuestionEvaluation.builder()
                .answer(answer)
                .score(80)
                .strengths("핵심 요소를 설명함")
                .weaknesses("예시 부족")
                .improvementSuggestion("격리 수준 예시 추가")
                .reasoning("기본 개념이 정확함")
                .aiModel("test")
                .build());

        mockMvc.perform(get("/api/v1/admin/interviews")
                        .header("Authorization", bearer(admin))
                        .param("status", "READY")
                        .param("userEmail", "owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].questionCount").value(1))
                .andExpect(jsonPath("$.data.content[0].answeredQuestionCount").value(1));

        mockMvc.perform(get("/api/v1/admin/interviews/{interviewId}", interview.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userEmail").value(owner.getEmail()))
                .andExpect(jsonPath("$.data.questions[0].answerExists").value(true))
                .andExpect(jsonPath("$.data.questions[0].evaluationExists").value(true))
                .andExpect(jsonPath("$.data.questions[0].answerContent").doesNotExist());
    }

    @Test
    void dashboardUsesDatabaseAggregates() throws Exception {
        User admin = saveUser("admin@example.com", UserRole.ADMIN);
        User owner = saveUser("owner@example.com", UserRole.USER);
        interviewRepository.save(Interview.builder()
                .user(owner)
                .title("대기 면접")
                .status(InterviewStatus.READY)
                .build());
        Interview inProgress = Interview.builder()
                .user(owner)
                .title("진행 면접")
                .status(InterviewStatus.READY)
                .build();
        inProgress.start();
        interviewRepository.save(inProgress);

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.totalInterviews").value(2))
                .andExpect(jsonPath("$.data.readyInterviews").value(1))
                .andExpect(jsonPath("$.data.inProgressInterviews").value(1))
                .andExpect(jsonPath("$.data.todayCreatedInterviews").value(2))
                .andExpect(jsonPath("$.data.recentInterviews.length()").value(2));
    }

    private User saveUser(String email, UserRole role) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(email.substring(0, email.indexOf('@')))
                .role(role)
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .build());
    }

    private String bearer(User user) {
        return "Bearer " + jwtProvider.createAccessToken(user.getId(), user.getRole());
    }
}
