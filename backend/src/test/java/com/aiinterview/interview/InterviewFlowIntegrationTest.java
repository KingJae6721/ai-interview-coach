package com.aiinterview.interview;

import com.aiinterview.ai.dto.InterviewFeedbackResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.auth.JwtProvider;
import com.aiinterview.company.entity.Company;
import com.aiinterview.company.repository.CompanyRepository;
import com.aiinterview.evaluation.entity.QuestionEvaluation;
import com.aiinterview.evaluation.repository.QuestionEvaluationRepository;
import com.aiinterview.feedback.repository.FeedbackRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import com.aiinterview.jobposting.repository.JobPostingAnalysisRepository;
import com.aiinterview.jobposting.repository.JobPostingRepository;
import com.aiinterview.user.entity.AuthProvider;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Testcontainers
@SpringBootTest
class InterviewFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ai_interview")
            .withUsername("postgres")
            .withPassword("postgres");

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

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private JobPositionRepository jobPositionRepository;
    @Autowired
    private JobPostingRepository jobPostingRepository;
    @Autowired
    private JobPostingAnalysisRepository jobPostingAnalysisRepository;
    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private InterviewQuestionRepository interviewQuestionRepository;
    @Autowired
    private InterviewAnswerRepository interviewAnswerRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private QuestionEvaluationRepository questionEvaluationRepository;

    @MockitoBean
    private AiService aiService;

    private User owner;
    private User otherUser;
    private JobPosition jobPosition;
    private JobPosting jobPosting;
    private String ownerToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        owner = createUser("owner@example.com", "owner");
        otherUser = createUser("other@example.com", "other");
        Company company = companyRepository.save(Company.builder().name("Example Corp").build());
        jobPosition = jobPositionRepository.save(JobPosition.builder()
                .company(company)
                .name("Backend Developer")
                .techStack(List.of("Java", "Spring Boot", "JPA"))
                .interviewCriteria("Explain design decisions with evidence.")
                .build());
        jobPosting = jobPostingRepository.save(JobPosting.builder()
                .user(owner)
                .jobPosition(jobPosition)
                .postingUrl("https://example.com/jobs/backend")
                .title("Backend Developer")
                .extractedContent("Saved posting snapshot")
                .build());
        jobPostingAnalysisRepository.save(JobPostingAnalysis.builder()
                .jobPosting(jobPosting)
                .companyName("Example Corp")
                .positionName("Backend Developer")
                .responsibilities(List.of("Build APIs"))
                .requiredQualifications(List.of("Java"))
                .preferredQualifications(List.of())
                .techStack(List.of("Java", "Spring Boot"))
                .experienceRequirements(List.of())
                .keywords(List.of("backend"))
                .summary("Backend role")
                .aiModel("test-model")
                .analyzedAt(LocalDateTime.now())
                .build());
        ownerToken = jwtProvider.createAccessToken(owner.getId(), owner.getRole());
        otherUserToken = jwtProvider.createAccessToken(otherUser.getId(), otherUser.getRole());

        given(aiService.generateInterviewQuestions(anyString()))
                .willReturn(List.of("Question 1", "Question 2", "Question 3", "Question 4", "Question 5"));
        given(aiService.generateFollowUpQuestion(anyString()))
                .willReturn(Optional.of("Follow-up question"));
        given(aiService.generateInterviewFeedback(any()))
                .willReturn(InterviewFeedbackResult.builder()
                        .overallScore(90)
                        .strengths("Strong technical reasoning")
                        .weaknesses("More concrete metrics needed")
                        .improvementSuggestions("Add measurable outcomes")
                        .summary("Well structured interview")
                        .aiModel("test-model")
                        .build());
    }

    @AfterEach
    void tearDown() {
        questionEvaluationRepository.deleteAll();
        feedbackRepository.deleteAll();
        interviewAnswerRepository.deleteAll();
        interviewQuestionRepository.deleteAll();
        interviewRepository.deleteAll();
        jobPostingAnalysisRepository.deleteAll();
        jobPostingRepository.deleteAll();
        jobPositionRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void interviewFlow_createToResult_enforcesAccessOrderAndCompletion() throws Exception {
        long interviewId = createInterview();
        assertThat(interviewRepository.findById(interviewId))
                .hasValueSatisfying(interview -> assertThat(interview.getStatus()).isEqualTo(InterviewStatus.READY));

        mockMvc.perform(get("/api/v1/interviews/{interviewId}", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.interviewId").value(interviewId))
                .andExpect(jsonPath("$.data.status").value("READY"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.startedAt").doesNotExist())
                .andExpect(jsonPath("$.data.completedAt").doesNotExist())
                .andExpect(jsonPath("$.data.jobPositionId").value(jobPosition.getId()))
                .andExpect(jsonPath("$.data.positionName").value("Backend Developer"))
                .andExpect(jsonPath("$.data.companyName").value("Example Corp"));
        assertThat(interviewRepository.findById(interviewId))
                .hasValueSatisfying(interview -> assertThat(interview.getStatus()).isEqualTo(InterviewStatus.READY));

        mockMvc.perform(get("/api/v1/interviews/{interviewId}", interviewId)
                        .header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/interviews/{interviewId}", Long.MAX_VALUE)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INTERVIEW_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/interviews/{interviewId}/questions", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
        assertThat(interviewRepository.findById(interviewId))
                .hasValueSatisfying(interview -> assertThat(interview.getStatus()).isEqualTo(InterviewStatus.READY));

        mockMvc.perform(get("/api/v1/interviews/{interviewId}/progress", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_NOT_STARTED"));

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.startedAt").exists());
        assertThat(interviewRepository.findById(interviewId))
                .hasValueSatisfying(interview -> {
                    assertThat(interview.getStatus()).isEqualTo(InterviewStatus.IN_PROGRESS);
                    assertThat(interview.getStartedAt()).isNotNull();
                });

        mockMvc.perform(get("/api/v1/interviews/{interviewId}", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.startedAt").exists())
                .andExpect(jsonPath("$.data.completedAt").doesNotExist());

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_ALREADY_STARTED"));

        JsonNode progress = getProgress(interviewId, ownerToken);
        long firstQuestionId = progress.path("questions").get(0).path("questionId").asLong();
        long secondQuestionId = progress.path("questions").get(1).path("questionId").asLong();
        assertThat(progress.path("nextQuestionId").asLong()).isEqualTo(firstQuestionId);
        assertThat(progress.path("allAnswered").asBoolean()).isFalse();

        mockMvc.perform(get("/api/v1/interviews/{interviewId}/progress", interviewId)
                        .header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        submitAnswer(interviewId, secondQuestionId, ownerToken, "out of order")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ANSWER_ORDER_INVALID"));

        submitAnswer(interviewId, firstQuestionId, ownerToken, "first answer")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
        submitAnswer(interviewId, firstQuestionId, ownerToken, "duplicate answer")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_ANSWER_ALREADY_EXISTS"));

        MvcResult followUpResult = mockMvc.perform(post("/api/v1/ai/questions/{questionId}/follow-up", firstQuestionId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.parentQuestionId").value(firstQuestionId))
                .andReturn();
        long followUpQuestionId = responseData(followUpResult).path("followUpQuestionId").asLong();

        JsonNode followUpProgress = getProgress(interviewId, ownerToken);
        assertThat(followUpProgress.path("questions").get(0).path("questionId").asLong()).isEqualTo(firstQuestionId);
        assertThat(followUpProgress.path("questions").get(1).path("questionId").asLong())
                .isEqualTo(followUpQuestionId);
        assertThat(followUpProgress.path("questions").get(2).path("questionId").asLong()).isEqualTo(secondQuestionId);
        assertThat(followUpProgress.path("nextQuestionId").asLong()).isEqualTo(followUpQuestionId);

        mockMvc.perform(post("/api/v1/ai/questions/{questionId}/follow-up", firstQuestionId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(false));

        mockMvc.perform(post("/api/v1/ai/questions/{questionId}/follow-up", followUpQuestionId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(false));
        then(aiService).should(times(1)).generateFollowUpQuestion(anyString());

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/complete", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_NOT_COMPLETABLE"))
                .andExpect(jsonPath("$.data.allAnswered").value(false))
                .andExpect(jsonPath("$.data.unansweredCount").isNumber())
                .andExpect(jsonPath("$.data.nextQuestionId").value(followUpQuestionId));

        submitAnswer(interviewId, followUpQuestionId, ownerToken, "follow-up answer")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
        assertThat(getProgress(interviewId, ownerToken).path("nextQuestionId").asLong()).isEqualTo(secondQuestionId);

        JsonNode currentProgress = getProgress(interviewId, ownerToken);
        while (!currentProgress.path("allAnswered").asBoolean()) {
            long nextQuestionId = currentProgress.path("nextQuestionId").asLong();
            submitAnswer(interviewId, nextQuestionId, ownerToken, "answer for " + nextQuestionId)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("SUCCESS"));
            currentProgress = getProgress(interviewId, ownerToken);
        }

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/complete", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists());
        assertThat(interviewRepository.findById(interviewId))
                .hasValueSatisfying(interview -> {
                    assertThat(interview.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
                    assertThat(interview.getCompletedAt()).isNotNull();
                });

        mockMvc.perform(get("/api/v1/interviews/{interviewId}", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.startedAt").exists())
                .andExpect(jsonPath("$.data.completedAt").exists());

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_ALREADY_COMPLETED"));

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/cancel", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_NOT_CANCELLABLE"));

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("AI_FEEDBACK_COMPLETED"));
        assertThat(feedbackRepository.existsByInterviewId(interviewId)).isTrue();

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FEEDBACK_ALREADY_EXISTS"));

        QuestionEvaluation evaluation = questionEvaluationRepository.save(QuestionEvaluation.builder()
                .answer(interviewAnswerRepository.findAllByInterviewIdWithQuestion(interviewId).get(0))
                .score(88)
                .strengths("Clear structure")
                .weaknesses("More specific examples needed")
                .improvementSuggestion("Add measurable outcomes")
                .reasoning("The answer covered the key concepts.")
                .aiModel("test-model")
                .build());

        mockMvc.perform(get("/api/v1/interviews/{interviewId}/result", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists())
                .andExpect(jsonPath("$.data.companyName").value("Example Corp"))
                .andExpect(jsonPath("$.data.positionName").value("Backend Developer"))
                .andExpect(jsonPath("$.data.questionAnswers.length()").value(6))
                .andExpect(jsonPath("$.data.questionAnswers[0].questionId").isNumber())
                .andExpect(jsonPath("$.data.questionAnswers[0].parentQuestionId").doesNotExist())
                .andExpect(jsonPath("$.data.questionAnswers[0].category").exists())
                .andExpect(jsonPath("$.data.questionAnswers[0].difficulty").exists())
                .andExpect(jsonPath("$.data.questionAnswers[0].followUp").value(false))
                .andExpect(jsonPath("$.data.questionAnswers[0].evaluation.evaluationId").value(evaluation.getId()))
                .andExpect(jsonPath("$.data.questionAnswers[0].evaluation.score").value(88))
                .andExpect(jsonPath("$.data.questionAnswers[1].parentQuestionId").value(firstQuestionId))
                .andExpect(jsonPath("$.data.questionAnswers[1].followUp").value(true))
                .andExpect(jsonPath("$.data.questionAnswers[1].evaluation").doesNotExist())
                .andExpect(jsonPath("$.data.feedback.overallScore").value(90));
        assertThat(interviewAnswerRepository.countByInterviewQuestionInterviewId(interviewId)).isEqualTo(6);

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cancelledInterviews").value(0))
                .andExpect(jsonPath("$.data.recentInterviews[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.recentInterviews[0].feedbackExists").value(true))
                .andExpect(jsonPath("$.data.recentInterviews[0].partial").value(false));

        InterviewQuestion evaluatedQuestion = interviewQuestionRepository.findById(firstQuestionId).orElseThrow();
        Interview completedInterview = interviewRepository.findById(interviewId).orElseThrow();
        interviewQuestionRepository.save(InterviewQuestion.builder()
                .interview(completedInterview)
                .questionOrder(100)
                .content("Unevaluated question")
                .category(evaluatedQuestion.getCategory())
                .difficulty(evaluatedQuestion.getDifficulty())
                .type(evaluatedQuestion.getType())
                .isAiGenerated(true)
                .build());

        MvcResult weaknessResult = mockMvc.perform(get("/api/v1/dashboard/weaknesses")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.performanceAnalysisAvailable").value(true))
                .andReturn();
        JsonNode weaknessData = responseData(weaknessResult);
        JsonNode categoryStatistic = findDashboardStatistic(weaknessData.path("categoryStatistics"), "category",
                evaluatedQuestion.getCategory().name());
        assertThat(categoryStatistic.path("questionCount").asLong()).isEqualTo(2);
        assertThat(categoryStatistic.path("evaluationCount").asLong()).isEqualTo(1);
        JsonNode difficultyStatistic = findDashboardStatistic(weaknessData.path("difficultyStatistics"), "difficulty",
                evaluatedQuestion.getDifficulty().name());
        assertThat(difficultyStatistic.path("questionCount").asLong()).isGreaterThan(1);
        assertThat(difficultyStatistic.path("evaluationCount").asLong()).isEqualTo(1);
    }

    @Test
    void cancelledInterview_generatesPartialFeedbackWithoutAffectingScoreStatistics() throws Exception {
        long interviewId = createInterview();

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        JsonNode progress = getProgress(interviewId, ownerToken);
        long firstQuestionId = progress.path("questions").get(0).path("questionId").asLong();
        long secondQuestionId = progress.path("questions").get(1).path("questionId").asLong();
        submitAnswer(interviewId, firstQuestionId, ownerToken, "first answer")
                .andExpect(status().isCreated());
        submitAnswer(interviewId, secondQuestionId, ownerToken, "second answer")
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/cancel", interviewId)
                        .header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/cancel", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelledAt").exists());
        assertThat(interviewRepository.findById(interviewId))
                .hasValueSatisfying(interview -> {
                    assertThat(interview.getStatus()).isEqualTo(InterviewStatus.CANCELLED);
                    assertThat(interview.getCancelledAt()).isNotNull();
                    assertThat(interview.getCompletedAt()).isNull();
                });

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/cancel", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTERVIEW_NOT_CANCELLABLE"));

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.partial").value(true))
                .andExpect(jsonPath("$.data.answeredCount").value(2))
                .andExpect(jsonPath("$.data.totalQuestionCount").value(5))
                .andExpect(jsonPath("$.data.overallScore").doesNotExist());
        ArgumentCaptor<com.aiinterview.ai.dto.InterviewFeedbackRequest> feedbackRequestCaptor =
                ArgumentCaptor.forClass(com.aiinterview.ai.dto.InterviewFeedbackRequest.class);
        then(aiService).should().generateInterviewFeedback(feedbackRequestCaptor.capture());
        assertThat(feedbackRequestCaptor.getValue().isPartial()).isTrue();
        assertThat(feedbackRequestCaptor.getValue().getQuestionAnswers()).hasSize(2);
        assertThat(feedbackRepository.findByInterviewId(interviewId))
                .hasValueSatisfying(feedback -> {
                    assertThat(feedback.isPartial()).isTrue();
                    assertThat(feedback.getOverallScore()).isNull();
                });

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FEEDBACK_ALREADY_EXISTS"));

        mockMvc.perform(get("/api/v1/interviews/{interviewId}/result", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelledAt").exists())
                .andExpect(jsonPath("$.data.feedback.partial").value(true))
                .andExpect(jsonPath("$.data.feedback.answeredCount").value(2))
                .andExpect(jsonPath("$.data.feedback.overallScore").doesNotExist())
                .andExpect(jsonPath("$.data.questionAnswers.length()").value(5));

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalInterviews").value(1))
                .andExpect(jsonPath("$.data.completedInterviews").value(0))
                .andExpect(jsonPath("$.data.cancelledInterviews").value(1))
                .andExpect(jsonPath("$.data.recentInterviews[0].cancelledAt").exists())
                .andExpect(jsonPath("$.data.recentInterviews[0].feedbackExists").value(true))
                .andExpect(jsonPath("$.data.recentInterviews[0].partial").value(true))
                .andExpect(jsonPath("$.data.averageScore").doesNotExist())
                .andExpect(jsonPath("$.data.highestScore").doesNotExist());
    }

    @Test
    void cancelledInterview_withFewerThanTwoAnswers_cannotGeneratePartialFeedback() throws Exception {
        long interviewId = createInterview();

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/interviews/{interviewId}/cancel", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PARTIAL_FEEDBACK_GENERATION_NOT_AVAILABLE"));

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentInterviews[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.recentInterviews[0].cancelledAt").exists())
                .andExpect(jsonPath("$.data.recentInterviews[0].feedbackExists").value(false))
                .andExpect(jsonPath("$.data.recentInterviews[0].partial").value(false));
    }

    @Test
    void interviewHistory_returnsLifecycleAndFeedbackMetadataInCreatedAtDescendingOrder() throws Exception {
        long readyInterviewId = createInterview();

        long inProgressInterviewId = createInterview();
        startInterview(inProgressInterviewId);

        long cancelledWithoutFeedbackInterviewId = createInterview();
        startInterview(cancelledWithoutFeedbackInterviewId);
        cancelInterview(cancelledWithoutFeedbackInterviewId);

        long cancelledWithPartialFeedbackInterviewId = createInterview();
        startInterview(cancelledWithPartialFeedbackInterviewId);
        JsonNode partialProgress = getProgress(cancelledWithPartialFeedbackInterviewId, ownerToken);
        submitAnswer(cancelledWithPartialFeedbackInterviewId,
                partialProgress.path("questions").get(0).path("questionId").asLong(), ownerToken, "first answer")
                .andExpect(status().isCreated());
        submitAnswer(cancelledWithPartialFeedbackInterviewId,
                partialProgress.path("questions").get(1).path("questionId").asLong(), ownerToken, "second answer")
                .andExpect(status().isCreated());
        cancelInterview(cancelledWithPartialFeedbackInterviewId);
        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", cancelledWithPartialFeedbackInterviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isCreated());

        long completedInterviewId = createInterview();
        startInterview(completedInterviewId);
        JsonNode completedProgress = getProgress(completedInterviewId, ownerToken);
        while (!completedProgress.path("allAnswered").asBoolean()) {
            long questionId = completedProgress.path("nextQuestionId").asLong();
            submitAnswer(completedInterviewId, questionId, ownerToken, "answer for " + questionId)
                    .andExpect(status().isCreated());
            completedProgress = getProgress(completedInterviewId, ownerToken);
        }
        mockMvc.perform(post("/api/v1/interviews/{interviewId}/complete", completedInterviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/interviews/{interviewId}/feedback", completedInterviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isCreated());

        MvcResult historyResult = mockMvc.perform(get("/api/v1/interviews?page=0&size=10")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.content[0].interviewId").value(completedInterviewId))
                .andReturn();

        JsonNode content = responseData(historyResult).path("content");
        JsonNode ready = findHistoryItem(content, readyInterviewId);
        assertThat(ready.path("status").asText()).isEqualTo("READY");
        assertThat(ready.path("startedAt").isNull()).isTrue();
        assertThat(ready.path("cancelledAt").isNull()).isTrue();
        assertThat(ready.path("feedbackExists").asBoolean()).isFalse();
        assertThat(ready.path("partial").asBoolean()).isFalse();

        JsonNode inProgress = findHistoryItem(content, inProgressInterviewId);
        assertThat(inProgress.path("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(inProgress.path("startedAt").asText()).isNotBlank();
        assertThat(inProgress.path("cancelledAt").isNull()).isTrue();
        assertThat(inProgress.path("feedbackExists").asBoolean()).isFalse();

        JsonNode cancelledWithoutFeedback = findHistoryItem(content, cancelledWithoutFeedbackInterviewId);
        assertThat(cancelledWithoutFeedback.path("status").asText()).isEqualTo("CANCELLED");
        assertThat(cancelledWithoutFeedback.path("cancelledAt").asText()).isNotBlank();
        assertThat(cancelledWithoutFeedback.path("feedbackExists").asBoolean()).isFalse();
        assertThat(cancelledWithoutFeedback.path("partial").asBoolean()).isFalse();

        JsonNode cancelledWithPartialFeedback = findHistoryItem(content, cancelledWithPartialFeedbackInterviewId);
        assertThat(cancelledWithPartialFeedback.path("status").asText()).isEqualTo("CANCELLED");
        assertThat(cancelledWithPartialFeedback.path("cancelledAt").asText()).isNotBlank();
        assertThat(cancelledWithPartialFeedback.path("feedbackExists").asBoolean()).isTrue();
        assertThat(cancelledWithPartialFeedback.path("partial").asBoolean()).isTrue();

        JsonNode completed = findHistoryItem(content, completedInterviewId);
        assertThat(completed.path("status").asText()).isEqualTo("COMPLETED");
        assertThat(completed.path("completedAt").asText()).isNotBlank();
        assertThat(completed.path("feedbackExists").asBoolean()).isTrue();
        assertThat(completed.path("partial").asBoolean()).isFalse();
    }

    private long createInterview() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/interviews")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobPostingId\":" + jobPosting.getId() + ",\"title\":\"Backend Interview\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("INTERVIEW_CREATED"))
                .andExpect(jsonPath("$.data.questionCount").value(5))
                .andReturn();
        return responseData(result).path("interviewId").asLong();
    }

    private JsonNode getProgress(long interviewId, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/interviews/{interviewId}/progress", interviewId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();
        return responseData(result);
    }

    private void startInterview(long interviewId) throws Exception {
        mockMvc.perform(post("/api/v1/interviews/{interviewId}/start", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
    }

    private void cancelInterview(long interviewId) throws Exception {
        mockMvc.perform(post("/api/v1/interviews/{interviewId}/cancel", interviewId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
    }

    private JsonNode findHistoryItem(JsonNode content, long interviewId) {
        for (JsonNode item : content) {
            if (item.path("interviewId").asLong() == interviewId) {
                return item;
            }
        }
        throw new AssertionError("Interview history item not found: " + interviewId);
    }

    private JsonNode findDashboardStatistic(JsonNode statistics, String fieldName, String expectedValue) {
        for (JsonNode statistic : statistics) {
            if (expectedValue.equals(statistic.path(fieldName).asText())) {
                return statistic;
            }
        }
        throw new AssertionError("Dashboard statistic not found: " + expectedValue);
    }

    private org.springframework.test.web.servlet.ResultActions submitAnswer(long interviewId, long questionId,
                                                                             String token, String answerContent)
            throws Exception {
        return mockMvc.perform(post("/api/v1/interviews/questions/{questionId}/answers", questionId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answerContent\":\"" + answerContent + "\"}"));
    }

    private JsonNode responseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private User createUser(String email, String nickname) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .build());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
