package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.QuestionEvaluationRequest;
import com.aiinterview.ai.provider.AiProvider;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceImplTest {

    @Mock
    private AiProvider aiProvider;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        lenient().when(aiProvider.getModel()).thenReturn("test-model");
        aiService = new OpenAiServiceImpl(new ObjectMapper(), aiProvider);
    }

    @Test
    void generateInterviewQuestions_parsesJsonCodeFence() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"```json\\n[\\"Q1\\", \\"Q2\\", \\"Q3\\", \\"Q4\\", \\"Q5\\"]\\n```"}}]}
                """);

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
    void generateInterviewFeedback_parsesStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\\"overallScore\\":90,\\"strengths\\":\\"strength\\",\\"weaknesses\\":\\"weakness\\",\\"improvementSuggestions\\":\\"suggestion\\",\\"summary\\":\\"summary\\"}"}}]}
                """);

        assertThat(aiService.generateInterviewFeedback(InterviewFeedbackRequest.builder()
                .interviewTitle("title")
                .questionAnswers(List.of())
                .build()).getOverallScore()).isEqualTo(90);
    }

    @Test
    void evaluateQuestionAnswer_parsesStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\\"score\\":80,\\"strengths\\":\\"strength\\",\\"weaknesses\\":\\"weakness\\",\\"improvementSuggestion\\":\\"suggestion\\",\\"reasoning\\":\\"reasoning\\"}"}}]}
                """);

        assertThat(aiService.evaluateQuestionAnswer(QuestionEvaluationRequest.builder()
                .questionContent("question")
                .answerContent("answer")
                .category(InterviewQuestionCategory.CS)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .build()).getScore()).isEqualTo(80);
    }

    @Test
    void analyzeJobPosting_parsesStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"```json\\n{\\"companyName\\":\\"Example Corp\\",\\"positionName\\":\\"Backend Developer\\",\\"responsibilities\\":[\\"Build APIs\\"],\\"requiredQualifications\\":[\\"Java\\"],\\"preferredQualifications\\":[],\\"techStack\\":[\\"Spring Boot\\"],\\"experienceRequirements\\":[],\\"keywords\\":[\\"backend\\"],\\"summary\\":\\"Backend role\\"}\\n```"}}]}
                """);

        assertThat(aiService.analyzeJobPosting("posting content"))
                .satisfies(result -> {
                    assertThat(result.getCompanyName()).isEqualTo("Example Corp");
                    assertThat(result.getTechStack()).containsExactly("Spring Boot");
                });
    }

    @Test
    void analyzeJobPosting_throwsAiRequestFailedForInvalidStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\\"companyName\\":\\"Example Corp\\"}"}}]}
                """);

        assertThatThrownBy(() -> aiService.analyzeJobPosting("posting content"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void analyzeResume_parsesStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\\"summary\\":\\"Backend engineer\\",\\"skills\\":[\\"Java\\"],\\"workExperiences\\":[],\\"projects\\":[\\"API project\\"],\\"education\\":[],\\"certifications\\":[],\\"achievements\\":[\\"30% improvement\\"],\\"strengths\\":[\\"Problem solving\\"],\\"keywords\\":[\\"backend\\"]}"}}]}
                """);

        assertThat(aiService.analyzeResume("resume text")).satisfies(result -> {
            assertThat(result.getSkills()).containsExactly("Java");
            assertThat(result.getAchievements()).containsExactly("30% improvement");
        });
    }

    @Test
    void analyzeResume_throwsAiRequestFailedForInvalidStructuredResponse() {
        given(aiProvider.complete(any())).willReturn("""
                {"choices":[{"message":{"content":"{\\"summary\\":\\"Only summary\\"}"}}]}
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
}
