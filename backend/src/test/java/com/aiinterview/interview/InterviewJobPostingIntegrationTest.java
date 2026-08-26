package com.aiinterview.interview;

import com.aiinterview.ai.service.AiService;
import com.aiinterview.auth.JwtProvider;
import com.aiinterview.company.entity.Company;
import com.aiinterview.company.repository.CompanyRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import com.aiinterview.jobposting.fetch.JobPostingContentFetcher;
import com.aiinterview.jobposting.repository.JobPostingAnalysisRepository;
import com.aiinterview.jobposting.repository.JobPostingRepository;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class InterviewJobPostingIntegrationTest {

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
    @Autowired private CompanyRepository companyRepository;
    @Autowired private JobPositionRepository jobPositionRepository;
    @Autowired private JobPostingRepository jobPostingRepository;
    @Autowired private JobPostingAnalysisRepository jobPostingAnalysisRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private InterviewQuestionRepository interviewQuestionRepository;
    @MockitoBean private AiService aiService;
    @MockitoBean private JobPostingContentFetcher jobPostingContentFetcher;

    private MockMvc mockMvc;
    private JobPosition jobPosition;
    private String token;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        User user = userRepository.save(User.builder().email("interview-posting@example.com").password("password")
                .nickname("posting").role(UserRole.USER).authProvider(AuthProvider.LOCAL).status(UserStatus.ACTIVE).build());
        Company company = companyRepository.save(Company.builder().name("Example Corp").build());
        jobPosition = jobPositionRepository.save(JobPosition.builder().company(company).name("Backend Developer")
                .techStack(List.of("Java", "Spring Boot")).interviewCriteria("Explain trade-offs.").build());
        token = jwtProvider.createAccessToken(user.getId(), user.getRole());
        given(aiService.generateInterviewQuestions(anyString()))
                .willReturn(List.of("Q1", "Q2", "Q3", "Q4", "Q5"));
    }

    @AfterEach
    void tearDown() {
        interviewQuestionRepository.deleteAll();
        interviewRepository.deleteAll();
        jobPostingAnalysisRepository.deleteAll();
        jobPostingRepository.deleteAll();
        jobPositionRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createInterview_withoutJobPosting_preservesExistingFlow() throws Exception {
        long interviewId = createInterview(jobPosition.getId(), null);

        assertThat(interviewRepository.findById(interviewId)).hasValueSatisfying(interview ->
                assertThat(interview.getJobPosting()).isNull());
    }

    @Test
    void createInterview_withAnalyzedJobPosting_savesRelationAndAddsSnapshotToPrompt() throws Exception {
        JobPosting jobPosting = createJobPosting(jobPosition, true);

        long interviewId = createInterview(jobPosition.getId(), jobPosting.getId());

        assertThat(interviewRepository.findById(interviewId)).hasValueSatisfying(interview ->
                assertThat(interview.getJobPosting().getId()).isEqualTo(jobPosting.getId()));
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        then(aiService).should().generateInterviewQuestions(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("Build resilient APIs", "Java proficiency", "Cloud experience", "Java, PostgreSQL",
                        "3+ years", "reliability, transactions");
        then(jobPostingContentFetcher).shouldHaveNoInteractions();
        then(aiService).should(never()).analyzeJobPosting(anyString());
    }

    @Test
    void createInterview_rejectsUnknownJobPosting() throws Exception {
        performCreate(jobPosition.getId(), 999999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_POSTING_NOT_FOUND"));
        then(aiService).should(never()).generateInterviewQuestions(anyString());
    }

    @Test
    void createInterview_rejectsJobPostingFromAnotherPosition() throws Exception {
        JobPosition otherPosition = jobPositionRepository.save(JobPosition.builder()
                .company(jobPosition.getCompany()).name("Frontend Developer").build());
        JobPosting jobPosting = createJobPosting(otherPosition, true);

        performCreate(jobPosition.getId(), jobPosting.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("JOB_POSTING_POSITION_MISMATCH"));
    }

    @Test
    void createInterview_rejectsJobPostingWithoutAnalysis() throws Exception {
        JobPosting jobPosting = createJobPosting(jobPosition, false);

        performCreate(jobPosition.getId(), jobPosting.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("JOB_POSTING_NOT_ANALYZED"));
    }

    private long createInterview(Long positionId, Long postingId) throws Exception {
        MvcResult result = performCreate(positionId, postingId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.questionCount").value(5))
                .andReturn();
        return tools.jackson.databind.json.JsonMapper.builder().build()
                .readTree(result.getResponse().getContentAsString()).at("/data/interviewId").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions performCreate(Long positionId, Long postingId)
            throws Exception {
        String optionalPosting = postingId == null ? "" : ",\"jobPostingId\":" + postingId;
        return mockMvc.perform(post("/api/v1/interviews")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"jobPositionId\":" + positionId + optionalPosting + ",\"title\":\"Posting Interview\"}"));
    }

    private JobPosting createJobPosting(JobPosition position, boolean analyzed) {
        JobPosting posting = jobPostingRepository.save(JobPosting.builder().jobPosition(position)
                .postingUrl("https://example.com/jobs/" + position.getId()).title("Backend role")
                .extractedContent("Saved posting snapshot").build());
        if (analyzed) {
            jobPostingAnalysisRepository.save(JobPostingAnalysis.builder().jobPosting(posting)
                    .companyName("Example Corp").positionName(position.getName())
                    .responsibilities(List.of("Build resilient APIs"))
                    .requiredQualifications(List.of("Java proficiency"))
                    .preferredQualifications(List.of("Cloud experience"))
                    .techStack(List.of("Java", "PostgreSQL"))
                    .experienceRequirements(List.of("3+ years"))
                    .keywords(List.of("reliability", "transactions"))
                    .summary("Backend reliability role").aiModel("test-model").analyzedAt(LocalDateTime.now()).build());
        }
        return posting;
    }
}
