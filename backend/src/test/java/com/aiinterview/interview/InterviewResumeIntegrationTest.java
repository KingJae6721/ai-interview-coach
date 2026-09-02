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
import com.aiinterview.resume.entity.Resume;
import com.aiinterview.resume.entity.ResumeAnalysis;
import com.aiinterview.resume.extract.ResumeTextExtractor;
import com.aiinterview.resume.repository.ResumeAnalysisRepository;
import com.aiinterview.resume.repository.ResumeRepository;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
class InterviewResumeIntegrationTest {

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
    @Autowired private ResumeRepository resumeRepository;
    @Autowired private ResumeAnalysisRepository resumeAnalysisRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private InterviewQuestionRepository interviewQuestionRepository;
    @MockitoBean private AiService aiService;
    @MockitoBean private JobPostingContentFetcher jobPostingContentFetcher;
    @MockitoBean private ResumeTextExtractor resumeTextExtractor;

    private MockMvc mockMvc;
    private User owner;
    private User otherUser;
    private JobPosition jobPosition;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        owner = createUser("resume-owner@example.com", "owner");
        otherUser = createUser("resume-other@example.com", "other");
        Company company = companyRepository.save(Company.builder().name("Example Corp").build());
        jobPosition = jobPositionRepository.save(JobPosition.builder().company(company).name("Backend Developer")
                .techStack(List.of("Java", "Spring Boot")).interviewCriteria("Explain trade-offs.").build());
        ownerToken = jwtProvider.createAccessToken(owner.getId(), owner.getRole());
        given(aiService.generateInterviewQuestions(anyString()))
                .willReturn(List.of("Q1", "Q2", "Q3", "Q4", "Q5"));
    }

    @AfterEach
    void tearDown() {
        interviewQuestionRepository.deleteAll();
        interviewRepository.deleteAll();
        jobPostingAnalysisRepository.deleteAll();
        jobPostingRepository.deleteAll();
        resumeAnalysisRepository.deleteAll();
        resumeRepository.deleteAll();
        jobPositionRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createInterview_supportsJobPostingWithOptionalResumeAndUsesSavedSnapshots() throws Exception {
        JobPosting jobPosting = createJobPosting();
        Resume resume = createResume(owner, true);

        long postingInterviewId = createInterview(jobPosting.getId(), null);
        long combinedInterviewId = createInterview(jobPosting.getId(), resume.getId());

        assertRelations(postingInterviewId, jobPosting.getId(), null);
        assertRelations(combinedInterviewId, jobPosting.getId(), resume.getId());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        then(aiService).should(org.mockito.Mockito.times(2)).generateInterviewQuestions(promptCaptor.capture());
        List<String> prompts = promptCaptor.getAllValues();
        assertThat(prompts.get(0)).contains("[JOB POSTING CONTEXT]", "Build resilient APIs", "Kafka")
                .doesNotContain("[RESUME CONTEXT]");
        assertThat(prompts.get(1)).contains("[JOB POSTING CONTEXT]", "[RESUME CONTEXT]",
                "Build resilient APIs", "Kafka", "Java, Redis", "Cache project");

        then(jobPostingContentFetcher).shouldHaveNoInteractions();
        then(resumeTextExtractor).shouldHaveNoInteractions();
        then(aiService).should(never()).analyzeJobPosting(anyString());
        then(aiService).should(never()).analyzeResume(anyString());
    }

    @Test
    void createInterview_allowsOwnedAnalyzedResume() throws Exception {
        JobPosting jobPosting = createJobPosting();
        Resume resume = createResume(owner, true);

        performCreate(jobPosting.getId(), resume.getId())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.questionCount").value(5));
    }

    @Test
    void createInterview_rejectsAnotherUsersResume() throws Exception {
        JobPosting jobPosting = createJobPosting();
        Resume resume = createResume(otherUser, true);

        performCreate(jobPosting.getId(), resume.getId())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RESUME_ACCESS_DENIED"));
        then(aiService).should(never()).generateInterviewQuestions(anyString());
    }

    @Test
    void createInterview_rejectsUnknownResume() throws Exception {
        JobPosting jobPosting = createJobPosting();

        performCreate(jobPosting.getId(), 999999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESUME_NOT_FOUND"));
        then(aiService).should(never()).generateInterviewQuestions(anyString());
    }

    @Test
    void createInterview_rejectsResumeWithoutAnalysis() throws Exception {
        JobPosting jobPosting = createJobPosting();
        Resume resume = createResume(owner, false);

        performCreate(jobPosting.getId(), resume.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESUME_NOT_ANALYZED"));
        then(aiService).should(never()).generateInterviewQuestions(anyString());
    }

    private long createInterview(Long jobPostingId, Long resumeId) throws Exception {
        MvcResult result = performCreate(jobPostingId, resumeId)
                .andExpect(status().isCreated())
                .andReturn();
        return JsonMapper.builder().build().readTree(result.getResponse().getContentAsString())
                .at("/data/interviewId").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions performCreate(Long jobPostingId, Long resumeId)
            throws Exception {
        String resumeField = resumeId == null ? "" : ",\"resumeId\":" + resumeId;
        return mockMvc.perform(post("/api/v1/interviews")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .content("{\"title\":\"Personalized Interview\",\"jobPostingId\":"
                        + jobPostingId + resumeField + "}"));
    }

    private void assertRelations(long interviewId, Long expectedJobPostingId, Long expectedResumeId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        assertThat(interview.getJobPosting() == null ? null : interview.getJobPosting().getId())
                .isEqualTo(expectedJobPostingId);
        assertThat(interview.getResume() == null ? null : interview.getResume().getId()).isEqualTo(expectedResumeId);
    }

    private User createUser(String email, String nickname) {
        return userRepository.save(User.builder().email(email).password("password").nickname(nickname)
                .role(UserRole.USER).authProvider(AuthProvider.LOCAL).status(UserStatus.ACTIVE).build());
    }

    private Resume createResume(User user, boolean analyzed) {
        Resume resume = resumeRepository.save(Resume.builder().user(user).originalFileName("resume.pdf")
                .fileSize(1024L).contentType("application/pdf").fileHash("a".repeat(64))
                .extractedText("private@example.com 010-1234-5678 raw PDF text").build());
        if (analyzed) {
            resumeAnalysisRepository.save(ResumeAnalysis.builder().resume(resume)
                    .summary("Backend engineer private@example.com 010-1234-5678")
                    .skills(List.of("Java", "Redis"))
                    .workExperiences(List.of("Built Spring APIs"))
                    .projects(List.of("Cache project"))
                    .education(List.of("Computer Science"))
                    .certifications(List.of("SQL certificate"))
                    .achievements(List.of("Reduced latency by 40%"))
                    .strengths(List.of("Problem solving"))
                    .keywords(List.of("backend", "performance"))
                    .aiModel("test-model").analyzedAt(LocalDateTime.now()).build());
        }
        return resume;
    }

    private JobPosting createJobPosting() {
        JobPosting posting = jobPostingRepository.save(JobPosting.builder().jobPosition(jobPosition)
                .postingUrl("https://example.com/jobs/backend").title("Backend role")
                .extractedContent("Saved posting snapshot").build());
        jobPostingAnalysisRepository.save(JobPostingAnalysis.builder().jobPosting(posting)
                .companyName("Example Corp").positionName(jobPosition.getName())
                .responsibilities(List.of("Build resilient APIs"))
                .requiredQualifications(List.of("Java proficiency"))
                .preferredQualifications(List.of("Kafka experience"))
                .techStack(List.of("Java", "Kafka"))
                .experienceRequirements(List.of("3+ years"))
                .keywords(List.of("reliability", "events"))
                .summary("Backend platform role").aiModel("test-model").analyzedAt(LocalDateTime.now()).build());
        return posting;
    }
}
