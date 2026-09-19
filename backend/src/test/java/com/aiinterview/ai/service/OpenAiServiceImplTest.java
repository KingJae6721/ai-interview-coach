package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.QuestionEvaluationRequest;
import com.aiinterview.ai.dto.QuestionEvaluationResult;
import com.aiinterview.ai.provider.AiCompletionRequest;
import com.aiinterview.ai.provider.AiProvider;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceImplTest {

    private static final String SAMPLE_EXPERIENCE_QUESTION =
            "신입으로서 참여한 프로젝트에서 직면했던 가장 큰 기술적 도전은 무엇이었고, "
                    + "이를 해결하기 위해 어떤 절차와 도구를 사용했나요?";

    @Mock
    private AiProvider aiProvider;

    private AiService aiService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        lenient().when(aiProvider.getModel()).thenReturn("test-model");
        objectMapper = new ObjectMapper();
        aiService = new OpenAiServiceImpl(objectMapper, aiProvider);
    }

    @Test
    void generateInterviewQuestions_parsesJsonCodeFence() throws Exception {
        given(aiProvider.complete(any())).willReturn(completion("""
                ```json
                ["Q1", "Q2", "Q3", "Q4", "Q5"]
                ```
                """));

        assertThat(aiService.generateInterviewQuestions("prompt"))
                .containsExactly("Q1", "Q2", "Q3", "Q4", "Q5");
    }

    @Test
    void generateFollowUpQuestion_returnsEmptyForNoFollowUp() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"NO_FOLLOW_UP"}}]}
                """);

        assertThat(aiService.generateFollowUpQuestion("answer")).isEqualTo(Optional.empty());
    }

    @Test
    void generateInterviewFeedback_parsesStructuredResponse() throws Exception {
        given(aiProvider.complete(any())).willReturn(completion(objectMapper.writeValueAsString(Map.of(
                "overallScore", 90,
                "strengths", "strength",
                "weaknesses", "weakness",
                "improvementSuggestions", "suggestion",
                "summary", "summary"
        ))));

        assertThat(aiService.generateInterviewFeedback(InterviewFeedbackRequest.builder()
                .interviewTitle("title")
                .questionAnswers(List.of())
                .build()).getOverallScore()).isEqualTo(90);

        ArgumentCaptor<AiCompletionRequest> requestCaptor = ArgumentCaptor.forClass(AiCompletionRequest.class);
        then(aiProvider).should().complete(requestCaptor.capture());
        assertThat(requestCaptor.getValue().maxCompletionTokens()).isEqualTo(768);
    }

    @Test
    void generateInterviewFeedback_reportsOutputTokenLimitWithoutParsingTruncatedContent() throws Exception {
        given(aiProvider.complete(any())).willReturn(objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of(
                        "finish_reason", "length",
                        "message", Map.of("content", "{\"overallScore\":90")
                ))
        )));

        assertThatThrownBy(() -> aiService.generateInterviewFeedback(InterviewFeedbackRequest.builder()
                .interviewTitle("title")
                .questionAnswers(List.of())
                .build())).isInstanceOf(BusinessException.class);
    }

    @Test
    void evaluateQuestionAnswer_calculatesScoreFromCategoryCriteria() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, Map.of(
                "requirementFulfillment", 70,
                "technicalAccuracy", 80,
                "explanationAndEvidence", 60,
                "clarity", 90
        )));

        assertThat(evaluate("What is a transaction?", "A transaction is an atomic unit of work.",
                InterviewQuestionCategory.CS)).satisfies(result -> {
                    assertThat(result.getScore()).isEqualTo(74);
                    assertThat(result.getStrengths()).isEqualTo("strength");
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "1", "네", "모르겠습니다"})
    void evaluateQuestionAnswer_givesZeroWithoutProviderCallForClearlyInsufficientAnswer(String answer) {
        assertThat(evaluate(SAMPLE_EXPERIENCE_QUESTION, answer, InterviewQuestionCategory.EXPERIENCE))
                .satisfies(result -> {
                    assertThat(result.getScore()).isZero();
                    assertThat(result.getStrengths()).contains("강점");
                    assertThat(result.getWeaknesses()).contains("평가 가능한 답변");
                    assertThat(result.getImprovementSuggestion()).contains("핵심 요구사항");
                    assertThat(result.getReasoning()).contains("평가할 수 없습니다");
                });
        then(aiProvider).should(never()).complete(any());
    }

    @Test
    void evaluateQuestionAnswer_givesZeroWhenAiDeterminesAnswerIsUnrelated() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(false, csScores(20)));

        assertThat(evaluate("Explain transaction isolation.", "오늘 날씨가 좋습니다.",
                InterviewQuestionCategory.CS)).satisfies(result -> {
                    assertThat(result.getScore()).isZero();
                    assertThat(result.getStrengths()).isEqualTo("실질적인 강점을 확인할 수 없습니다.");
                });
    }

    @Test
    void evaluateQuestionAnswer_doesNotRejectShortButSubstantiveAnswer() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, csScores(85)));

        assertThat(evaluate("HTTP 404의 의미는?", "요청한 리소스를 찾을 수 없음",
                InterviewQuestionCategory.TECH_STACK).getScore()).isEqualTo(85);
        then(aiProvider).should().complete(any());
    }

    @Test
    void evaluateQuestionAnswer_calibratesPartialBasicAndExcellentScores() throws Exception {
        given(aiProvider.complete(any()))
                .willReturn(evaluationCompletion(true, csScores(30)))
                .willReturn(evaluationCompletion(true, csScores(55)))
                .willReturn(evaluationCompletion(true, csScores(90)));

        assertThat(evaluate("Explain optimistic locking.", "It uses a version.",
                InterviewQuestionCategory.CS).getScore()).isEqualTo(30);
        assertThat(evaluate("Explain optimistic locking.", "It detects concurrent updates with a version.",
                InterviewQuestionCategory.CS).getScore()).isEqualTo(55);
        assertThat(evaluate("Explain optimistic locking.",
                "It compares a version at update time and rejects stale writes without holding a database lock.",
                InterviewQuestionCategory.CS).getScore()).isEqualTo(90);
    }

    @Test
    void evaluateQuestionAnswer_penalizesExperienceAnswerMissingActionAndResult() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, Map.of(
                "requirementFulfillment", 50,
                "situationSpecificity", 70,
                "candidateAction", 0,
                "technicalJudgment", 10,
                "resultAndReflection", 0
        )));

        assertThat(evaluate("Describe a challenge, your action, and its result.",
                "The project had frequent deployment failures.", InterviewQuestionCategory.EXPERIENCE).getScore())
                .isEqualTo(23);
    }

    @Test
    void evaluateQuestionAnswer_doesNotRequireExperienceForTechnicalQuestion() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, csScores(88)));

        QuestionEvaluationResult result = evaluate("Explain database normalization.",
                "Normalization separates dependencies to reduce redundancy and update anomalies.",
                InterviewQuestionCategory.CS);

        assertThat(result.getScore()).isEqualTo(88);
        ArgumentCaptor<AiCompletionRequest> requestCaptor = ArgumentCaptor.forClass(AiCompletionRequest.class);
        then(aiProvider).should().complete(requestCaptor.capture());
        assertThat(requestCaptor.getValue().systemPrompt()).contains("Do not require experience unless the question asks");
        assertThat(requestCaptor.getValue().userPrompt())
                .contains("category=CS", "difficulty=MEDIUM", "technicalAccuracy");
        assertThat(requestCaptor.getValue().maxCompletionTokens()).isEqualTo(640);
        assertThat(requestCaptor.getValue().responseFormat().toString())
                .contains("sufficient", "criteria", "technicalAccuracy")
                .doesNotContain("score=");
    }

    @Test
    void evaluateQuestionAnswer_returnsSameScoreForSameStructuredAssessment() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, csScores(77)));
        QuestionEvaluationRequest request = evaluationRequest(
                "Explain a transaction.", "A transaction is an atomic unit of work.", InterviewQuestionCategory.CS);

        assertThat(aiService.evaluateQuestionAnswer(request).getScore())
                .isEqualTo(aiService.evaluateQuestionAnswer(request).getScore())
                .isEqualTo(77);
    }

    @Test
    void evaluateQuestionAnswer_rejectsCriterionOutsideScoreRange() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, Map.of(
                "requirementFulfillment", 101,
                "technicalAccuracy", 80,
                "explanationAndEvidence", 80,
                "clarity", 80
        )));

        assertThatThrownBy(() -> evaluate("question", "substantive answer", InterviewQuestionCategory.CS))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void evaluateQuestionAnswer_usesFallbackRubricForLegacyUncategorizedQuestion() throws Exception {
        given(aiProvider.complete(any())).willReturn(evaluationCompletion(true, Map.of(
                "requirementFulfillment", 70,
                "relevanceAndAccuracy", 70,
                "explanationAndEvidence", 70,
                "clarity", 70
        )));

        QuestionEvaluationResult result = aiService.evaluateQuestionAnswer(QuestionEvaluationRequest.builder()
                .questionContent("Legacy question")
                .answerContent("A substantive legacy answer")
                .category(null)
                .difficulty(null)
                .build());

        assertThat(result.getScore()).isEqualTo(70);
        ArgumentCaptor<AiCompletionRequest> requestCaptor = ArgumentCaptor.forClass(AiCompletionRequest.class);
        then(aiProvider).should().complete(requestCaptor.capture());
        assertThat(requestCaptor.getValue().userPrompt()).contains("category=UNSPECIFIED");
    }

    @Test
    void analyzeJobPosting_parsesStructuredResponse() throws Exception {
        String analysis = objectMapper.writeValueAsString(Map.of(
                "companyName", "Example Corp",
                "positionName", "Backend Developer",
                "responsibilities", List.of("Build APIs"),
                "requiredQualifications", List.of("Java"),
                "preferredQualifications", List.of(),
                "techStack", List.of("Spring Boot"),
                "experienceRequirements", List.of(),
                "keywords", List.of("backend"),
                "summary", "Backend role"
        ));
        given(aiProvider.complete(any())).willReturn(completion("```json\n" + analysis + "\n```"));

        assertThat(aiService.analyzeJobPosting("posting content"))
                .satisfies(result -> {
                    assertThat(result.getCompanyName()).isEqualTo("Example Corp");
                    assertThat(result.getTechStack()).containsExactly("Spring Boot");
                });
    }

    @Test
    void analyzeJobPosting_throwsAiRequestFailedForInvalidStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\"companyName\":\"Example Corp\"}"}}]}
                """);

        assertThatThrownBy(() -> aiService.analyzeJobPosting("posting content"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void analyzeResume_parsesStructuredResponse() throws Exception {
        given(aiProvider.complete(any())).willReturn(completion(objectMapper.writeValueAsString(Map.of(
                "summary", "Backend engineer",
                "skills", List.of("Java"),
                "workExperiences", List.of(),
                "projects", List.of("API project"),
                "education", List.of(),
                "certifications", List.of(),
                "achievements", List.of("30% improvement"),
                "strengths", List.of("Problem solving"),
                "keywords", List.of("backend")
        ))));

        assertThat(aiService.analyzeResume("resume text")).satisfies(result -> {
            assertThat(result.getSkills()).containsExactly("Java");
            assertThat(result.getAchievements()).containsExactly("30% improvement");
        });
    }

    @Test
    void analyzeResume_throwsAiRequestFailedForInvalidStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\"summary\":\"Only summary\"}"}}]}
                """);

        assertThatThrownBy(() -> aiService.analyzeResume("resume text"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void generateInterviewQuestions_throwsAiRequestFailedForInvalidJson() {
        given(aiProvider.complete(any())).willReturn("{" + "\"choices\":[]}");

        assertThatThrownBy(() -> aiService.generateInterviewQuestions("prompt"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void generateInterviewQuestions_propagatesProviderFailure() {
        given(aiProvider.complete(any())).willThrow(new BusinessException(
                com.aiinterview.common.code.ErrorCode.AI_REQUEST_FAILED));

        assertThatThrownBy(() -> aiService.generateInterviewQuestions("prompt"))
                .isInstanceOf(BusinessException.class);
    }

    private QuestionEvaluationResult evaluate(String question, String answer,
                                                InterviewQuestionCategory category) {
        return aiService.evaluateQuestionAnswer(evaluationRequest(question, answer, category));
    }

    private QuestionEvaluationRequest evaluationRequest(String question, String answer,
                                                         InterviewQuestionCategory category) {
        return QuestionEvaluationRequest.builder()
                .questionContent(question)
                .answerContent(answer)
                .category(category)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .build();
    }

    private Map<String, Integer> csScores(int score) {
        return Map.of(
                "requirementFulfillment", score,
                "technicalAccuracy", score,
                "explanationAndEvidence", score,
                "clarity", score
        );
    }

    private String evaluationCompletion(boolean sufficient, Map<String, Integer> criteria) throws Exception {
        String evaluation = objectMapper.writeValueAsString(Map.of(
                "sufficient", sufficient,
                "criteria", criteria,
                "strengths", "strength",
                "weaknesses", "weakness",
                "improvementSuggestion", "suggestion",
                "reasoning", "reasoning"
        ));
        return completion(evaluation);
    }

    private String completion(String content) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of("message", Map.of("content", content)))
        ));
    }
}
