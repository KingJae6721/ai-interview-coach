package com.aiinterview.dashboard;

import com.aiinterview.auth.JwtProvider;
import com.aiinterview.feedback.entity.Feedback;
import com.aiinterview.feedback.repository.FeedbackRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class DashboardAnalyticsIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ai_interview").withUsername("postgres").withPassword("postgres");
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired private WebApplicationContext context;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private FeedbackRepository feedbackRepository;

    private MockMvc mockMvc;
    private User owner;
    private String token;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        owner = createUser("analytics-owner@example.com");
        User otherUser = createUser("analytics-other@example.com");
        token = jwtProvider.createAccessToken(owner.getId(), owner.getRole());
        createCompletedInterview(owner, "Owner 70", 70);
        createCompletedInterview(owner, "Owner 90", 90);
        createCompletedInterview(otherUser, "Other 10", 10);
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        interviewRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void weeklyAnalytics_groupsByCompletedAtWithoutParameterizedDateTruncConflict() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/analytics")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "WEEKLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].averageScore").value(80.0))
                .andExpect(jsonPath("$.data[0].interviewCount").value(2));
    }

    @Test
    void monthlyAnalytics_usesHibernateTemporalTruncation() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/analytics")
                        .header("Authorization", "Bearer " + token)
                        .param("period", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].averageScore").value(80.0))
                .andExpect(jsonPath("$.data[0].interviewCount").value(2));
    }

    private User createUser(String email) {
        return userRepository.save(User.builder().email(email).password("password").nickname("analytics")
                .role(UserRole.USER).authProvider(AuthProvider.LOCAL).status(UserStatus.ACTIVE).build());
    }

    private void createCompletedInterview(User user, String title, int score) {
        Interview interview = Interview.builder().user(user).title(title).status(InterviewStatus.IN_PROGRESS).build();
        interview.complete();
        interviewRepository.save(interview);
        feedbackRepository.save(Feedback.builder().interview(interview).overallScore(score).partial(false)
                .answeredCount(5).totalQuestionCount(5).strengths("strengths").weaknesses("weaknesses")
                .improvementSuggestions("suggestions").summary("summary").aiModel("test-model").build());
    }
}
