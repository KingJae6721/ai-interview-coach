package com.aiinterview.jobposting;

import com.aiinterview.ai.dto.JobPostingAnalysisResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.auth.JwtProvider;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.company.entity.Company;
import com.aiinterview.company.repository.CompanyRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.fetch.FetchedJobPostingContent;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class JobPostingAnalyzeIntegrationTest {

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
    @MockitoBean private JobPostingContentFetcher jobPostingContentFetcher;
    @MockitoBean private AiService aiService;

    private MockMvc mockMvc;
    private JobPosition jobPosition;
    private String token;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        User user = userRepository.save(User.builder().email("posting@example.com").password("password")
                .nickname("posting").role(UserRole.USER).authProvider(AuthProvider.LOCAL).status(UserStatus.ACTIVE).build());
        Company company = companyRepository.save(Company.builder().name("Example Corp").build());
        jobPosition = jobPositionRepository.save(JobPosition.builder().company(company).name("Backend Developer").build());
        token = jwtProvider.createAccessToken(user.getId(), user.getRole());
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
    void analyzeJobPosting_savesPostingAndAnalysis() throws Exception {
        given(jobPostingContentFetcher.fetch(anyString()))
                .willReturn(new FetchedJobPostingContent("Backend role", "Build resilient Java APIs."));
        given(aiService.analyzeJobPosting(anyString())).willReturn(analysisResult());

        mockMvc.perform(post("/api/v1/job-postings/analyze").header("Authorization", bearer())
                        .contentType("application/json")
                        .content("{\"jobPositionId\":" + jobPosition.getId() + ",\"postingUrl\":\"https://example.com/jobs/1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.jobPositionId").value(jobPosition.getId()))
                .andExpect(jsonPath("$.data.techStack[0]").value("Java"));

        assertThat(jobPostingRepository.findAll()).singleElement()
                .satisfies(posting -> assertThat(posting.getJobPosition().getId()).isEqualTo(jobPosition.getId()));
        assertThat(jobPostingAnalysisRepository.findAll()).singleElement()
                .satisfies(analysis -> assertThat(analysis.getRequiredQualifications()).containsExactly("Java"));
    }

    @Test
    void analyzeJobPosting_withoutExistingReferences_createsAndReusesNormalizedCompanyAndPosition() throws Exception {
        jobPositionRepository.deleteAll();
        companyRepository.deleteAll();
        given(jobPostingContentFetcher.fetch(anyString()))
                .willReturn(new FetchedJobPostingContent("Backend role", "Build resilient Java APIs."));
        given(aiService.analyzeJobPosting(anyString()))
                .willReturn(analysisResult("Example Corp", "Backend Developer"),
                        analysisResult("  EXAMPLE   CORP  ", " backend   developer "));
        given(aiService.generateInterviewQuestions(anyString()))
                .willReturn(List.of("Q1", "Q2", "Q3", "Q4", "Q5"));

        MvcResult firstAnalysis = requestAnalyzeWithoutPosition("https://example.com/jobs/new-1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.jobPositionId").isNumber())
                .andReturn();
        long jobPostingId = tools.jackson.databind.json.JsonMapper.builder().build()
                .readTree(firstAnalysis.getResponse().getContentAsString())
                .at("/data/jobPostingId").asLong();

        MvcResult interviewCreation = mockMvc.perform(post("/api/v1/interviews").header("Authorization", bearer())
                        .contentType("application/json")
                        .content("{\"title\":\"New User Interview\",\"jobPostingId\":" + jobPostingId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.questionCount").value(5))
                .andReturn();
        long interviewId = tools.jackson.databind.json.JsonMapper.builder().build()
                .readTree(interviewCreation.getResponse().getContentAsString())
                .at("/data/interviewId").asLong();

        requestAnalyzeWithoutPosition("https://example.com/jobs/new-2")
                .andExpect(status().isCreated());

        assertThat(companyRepository.findAll()).singleElement().satisfies(company -> {
            assertThat(company.getName()).isEqualTo("Example Corp");
            assertThat(company.getNormalizedName()).isEqualTo("example corp");
        });
        assertThat(jobPositionRepository.findAll()).singleElement().satisfies(position -> {
            assertThat(position.getName()).isEqualTo("Backend Developer");
            assertThat(position.getNormalizedName()).isEqualTo("backend developer");
            assertThat(position.getTechStack()).containsExactly("Java", "Spring Boot");
        });
        assertThat(jobPostingRepository.count()).isEqualTo(2);
        assertThat(interviewRepository.findWithUserAndJobPositionAndCompanyById(interviewId))
                .hasValueSatisfying(interview -> {
            assertThat(interview.getJobPosting().getId()).isEqualTo(jobPostingId);
            assertThat(interview.getJobPosition().getNormalizedName()).isEqualTo("backend developer");
        });
    }

    @Test
    void analyzeJobPosting_withoutCompanyOrPosition_doesNotCreateArbitraryReferences() throws Exception {
        jobPositionRepository.deleteAll();
        companyRepository.deleteAll();
        given(jobPostingContentFetcher.fetch(anyString()))
                .willReturn(new FetchedJobPostingContent("Unknown role", "Insufficient content"));
        given(aiService.analyzeJobPosting(anyString())).willReturn(analysisResult(null, null));

        requestAnalyzeWithoutPosition("https://example.com/jobs/unknown")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("JOB_POSTING_ANALYSIS_INSUFFICIENT"));

        assertThat(companyRepository.count()).isZero();
        assertThat(jobPositionRepository.count()).isZero();
        assertThat(jobPostingRepository.count()).isZero();
    }

    @Test
    void analyzeJobPosting_rejectsUnknownJobPositionBeforeExternalCalls() throws Exception {
        mockMvc.perform(post("/api/v1/job-postings/analyze").header("Authorization", bearer())
                        .contentType("application/json")
                        .content("{\"jobPositionId\":999999,\"postingUrl\":\"https://example.com/jobs/1\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("JOB_POSITION_NOT_FOUND"));
        then(jobPostingContentFetcher).shouldHaveNoInteractions();
        then(aiService).shouldHaveNoInteractions();
    }

    @Test
    void analyzeJobPosting_returnsFetchAndContentErrors() throws Exception {
        given(jobPostingContentFetcher.fetch(anyString())).willThrow(new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED));
        requestAnalyze().andExpect(status().isBadGateway()).andExpect(jsonPath("$.code").value("JOB_POSTING_FETCH_FAILED"));
        reset(jobPostingContentFetcher);
        given(jobPostingContentFetcher.fetch(anyString())).willThrow(new BusinessException(ErrorCode.JOB_POSTING_CONTENT_NOT_FOUND));
        requestAnalyze().andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.code").value("JOB_POSTING_CONTENT_NOT_FOUND"));
    }

    @Test
    void analyzeJobPosting_propagatesAiFailureWithoutSaving() throws Exception {
        given(jobPostingContentFetcher.fetch(anyString())).willReturn(new FetchedJobPostingContent("title", "content"));
        given(aiService.analyzeJobPosting(anyString())).willThrow(new BusinessException(ErrorCode.AI_REQUEST_FAILED));
        requestAnalyze().andExpect(status().isBadGateway()).andExpect(jsonPath("$.code").value("AI_REQUEST_FAILED"));
        assertThat(jobPostingRepository.count()).isZero();
    }

    private org.springframework.test.web.servlet.ResultActions requestAnalyze() throws Exception {
        return mockMvc.perform(post("/api/v1/job-postings/analyze").header("Authorization", bearer())
                .contentType("application/json")
                .content("{\"jobPositionId\":" + jobPosition.getId() + ",\"postingUrl\":\"https://example.com/jobs/1\"}"));
    }

    private org.springframework.test.web.servlet.ResultActions requestAnalyzeWithoutPosition(String postingUrl)
            throws Exception {
        return mockMvc.perform(post("/api/v1/job-postings/analyze").header("Authorization", bearer())
                .contentType("application/json")
                .content("{\"postingUrl\":\"" + postingUrl + "\"}"));
    }

    private JobPostingAnalysisResult analysisResult() {
        return analysisResult("Example Corp", "Backend Developer");
    }

    private JobPostingAnalysisResult analysisResult(String companyName, String positionName) {
        return JobPostingAnalysisResult.builder().companyName(companyName).positionName(positionName)
                .responsibilities(List.of("Build APIs")).requiredQualifications(List.of("Java"))
                .preferredQualifications(List.of()).techStack(List.of("Java", "Spring Boot"))
                .experienceRequirements(List.of()).keywords(List.of("backend")).summary("Backend role")
                .aiModel("test-model").build();
    }

    private String bearer() { return "Bearer " + token; }
}
