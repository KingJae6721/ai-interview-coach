package com.aiinterview.evaluation.service;

import com.aiinterview.ai.dto.QuestionEvaluationRequest;
import com.aiinterview.ai.dto.QuestionEvaluationResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.evaluation.repository.QuestionEvaluationRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class QuestionEvaluationServiceImplTest {

    @Mock
    private InterviewAnswerRepository interviewAnswerRepository;
    @Mock
    private QuestionEvaluationRepository questionEvaluationRepository;
    @Mock
    private QuestionEvaluationPersistenceService questionEvaluationPersistenceService;
    @Mock
    private AiService aiService;

    private QuestionEvaluationServiceImpl questionEvaluationService;

    @BeforeEach
    void setUp() {
        questionEvaluationService = new QuestionEvaluationServiceImpl(
                interviewAnswerRepository,
                questionEvaluationRepository,
                questionEvaluationPersistenceService,
                aiService
        );
    }

    @Test
    void evaluateMissing_skipsExistingEvaluationAndEvaluatesOnlyMissingAnswer() {
        InterviewAnswer missingAnswer = answer(2L, 10L, "question", "answer");
        QuestionEvaluationResult result = evaluationResult();
        given(questionEvaluationRepository.findEvaluatedAnswerIdsByAnswerIdIn(List.of(1L, 2L)))
                .willReturn(Set.of(1L));
        given(interviewAnswerRepository.findAllWithQuestionInterviewAndUserByIdIn(List.of(2L)))
                .willReturn(List.of(missingAnswer));
        given(aiService.evaluateQuestionAnswer(any())).willReturn(result);

        questionEvaluationService.evaluateMissing(10L, List.of(1L, 2L));

        ArgumentCaptor<QuestionEvaluationRequest> requestCaptor =
                ArgumentCaptor.forClass(QuestionEvaluationRequest.class);
        then(aiService).should().evaluateQuestionAnswer(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getQuestionContent()).isEqualTo("question");
        assertThat(requestCaptor.getValue().getAnswerContent()).isEqualTo("answer");
        then(questionEvaluationPersistenceService).should().save(2L, result);
        then(questionEvaluationPersistenceService).should(never()).save(1L, result);
    }

    @Test
    void evaluateMissing_treatsConcurrentUniqueConflictAsAlreadyEvaluated() {
        InterviewAnswer answer = answer(1L, 10L, "question", "answer");
        given(questionEvaluationRepository.findEvaluatedAnswerIdsByAnswerIdIn(List.of(1L)))
                .willReturn(Set.of());
        given(interviewAnswerRepository.findAllWithQuestionInterviewAndUserByIdIn(List.of(1L)))
                .willReturn(List.of(answer));
        given(aiService.evaluateQuestionAnswer(any())).willReturn(evaluationResult());
        given(questionEvaluationPersistenceService.save(any(), any()))
                .willThrow(new BusinessException(ErrorCode.QUESTION_EVALUATION_ALREADY_EXISTS));

        assertThatCode(() -> questionEvaluationService.evaluateMissing(10L, List.of(1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void evaluateMissing_keepsCompletedWorkAndPropagatesAiFailure() {
        InterviewAnswer firstAnswer = answer(1L, 10L, "question 1", "answer 1");
        InterviewAnswer secondAnswer = answer(2L, 10L, "question 2", "answer 2");
        QuestionEvaluationResult result = evaluationResult();
        given(questionEvaluationRepository.findEvaluatedAnswerIdsByAnswerIdIn(List.of(1L, 2L)))
                .willReturn(Set.of());
        given(interviewAnswerRepository.findAllWithQuestionInterviewAndUserByIdIn(List.of(1L, 2L)))
                .willReturn(List.of(firstAnswer, secondAnswer));
        given(aiService.evaluateQuestionAnswer(any()))
                .willReturn(result)
                .willThrow(new BusinessException(ErrorCode.AI_REQUEST_FAILED));

        assertThatThrownBy(() -> questionEvaluationService.evaluateMissing(10L, List.of(1L, 2L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_REQUEST_FAILED));
        then(questionEvaluationPersistenceService).should().save(1L, result);
        then(questionEvaluationPersistenceService).should(times(1)).save(any(), any());
    }

    @Test
    void evaluateMissing_rejectsAnswerOwnedByAnotherUserBeforeAiCall() {
        InterviewAnswer answer = answer(1L, 20L, "question", "answer");
        given(questionEvaluationRepository.findEvaluatedAnswerIdsByAnswerIdIn(List.of(1L)))
                .willReturn(Set.of());
        given(interviewAnswerRepository.findAllWithQuestionInterviewAndUserByIdIn(List.of(1L)))
                .willReturn(List.of(answer));

        assertThatThrownBy(() -> questionEvaluationService.evaluateMissing(10L, List.of(1L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
        then(aiService).shouldHaveNoInteractions();
    }

    private InterviewAnswer answer(Long answerId, Long userId, String questionContent, String answerContent) {
        User user = mock(User.class);
        Interview interview = mock(Interview.class);
        InterviewQuestion question = mock(InterviewQuestion.class);
        InterviewAnswer answer = mock(InterviewAnswer.class);

        given(user.getId()).willReturn(userId);
        given(interview.getUser()).willReturn(user);
        given(question.getInterview()).willReturn(interview);
        lenient().when(question.getContent()).thenReturn(questionContent);
        lenient().when(question.getCategory()).thenReturn(InterviewQuestionCategory.TECH_STACK);
        lenient().when(question.getDifficulty()).thenReturn(InterviewQuestionDifficulty.MEDIUM);
        given(answer.getId()).willReturn(answerId);
        given(answer.getInterviewQuestion()).willReturn(question);
        lenient().when(answer.getAnswerContent()).thenReturn(answerContent);
        return answer;
    }

    private QuestionEvaluationResult evaluationResult() {
        return QuestionEvaluationResult.builder()
                .score(88)
                .strengths("strengths")
                .weaknesses("weaknesses")
                .improvementSuggestion("improvement")
                .reasoning("reasoning")
                .aiModel("test-model")
                .build();
    }
}
