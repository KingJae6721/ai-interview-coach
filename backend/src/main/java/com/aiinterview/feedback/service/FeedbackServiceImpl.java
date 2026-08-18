package com.aiinterview.feedback.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.InterviewFeedbackResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.evaluation.entity.QuestionEvaluation;
import com.aiinterview.evaluation.repository.QuestionEvaluationRepository;
import com.aiinterview.feedback.dto.FeedbackGenerateResponse;
import com.aiinterview.feedback.dto.InterviewResultFeedbackResponse;
import com.aiinterview.feedback.dto.InterviewResultQuestionAnswerResponse;
import com.aiinterview.feedback.dto.InterviewResultQuestionEvaluationResponse;
import com.aiinterview.feedback.dto.InterviewResultResponse;
import com.aiinterview.feedback.entity.Feedback;
import com.aiinterview.feedback.repository.FeedbackRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.interview.service.InterviewQuestionExecutionOrderResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final QuestionEvaluationRepository questionEvaluationRepository;
    private final InterviewQuestionExecutionOrderResolver interviewQuestionExecutionOrderResolver;
    private final AiService aiService;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FeedbackGenerateResponse generateFeedback(Long userId, Long interviewId) {
        InterviewFeedbackRequest request = prepareFeedbackRequest(userId, interviewId);
        InterviewFeedbackResult result = aiService.generateInterviewFeedback(request);
        Feedback feedback = saveFeedback(interviewId, result);

        log.info("Interview feedback generated - interviewId: {}, feedbackId: {}", interviewId, feedback.getId());

        return FeedbackGenerateResponse.builder()
                .feedbackId(feedback.getId())
                .interviewId(interviewId)
                .overallScore(feedback.getOverallScore())
                .strengths(feedback.getStrengths())
                .weaknesses(feedback.getWeaknesses())
                .improvementSuggestions(feedback.getImprovementSuggestions())
                .summary(feedback.getSummary())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResultResponse getInterviewResult(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findWithUserAndJobPositionAndCompanyById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        validateCompletedInterviewOwner(userId, interview);

        Feedback feedback = feedbackRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEEDBACK_NOT_FOUND));

        List<InterviewQuestion> questions = interviewQuestionExecutionOrderResolver.resolve(
                interviewQuestionRepository.findAllByInterviewIdWithParentOrderByQuestionOrderAsc(interviewId));
        List<InterviewAnswer> answers = interviewAnswerRepository.findAllByInterviewIdWithQuestion(interviewId);
        validateQuestionAnswers(questions, answers);

        Map<Long, InterviewAnswer> answerByQuestionId = answers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getInterviewQuestion().getId(),
                        Function.identity()
                ));

        Map<Long, QuestionEvaluation> evaluationByAnswerId = questionEvaluationRepository
                .findAllByInterviewIdWithAnswer(interviewId).stream()
                .collect(Collectors.toMap(evaluation -> evaluation.getAnswer().getId(), Function.identity()));

        List<InterviewResultQuestionAnswerResponse> questionAnswers = questions.stream()
                .map(question -> toQuestionAnswerResponse(question, answerByQuestionId.get(question.getId()),
                        evaluationByAnswerId.get(answerByQuestionId.get(question.getId()).getId())))
                .toList();

        return InterviewResultResponse.builder()
                .interviewId(interview.getId())
                .title(interview.getTitle())
                .status(interview.getStatus())
                .completedAt(interview.getCompletedAt())
                .companyName(interview.getJobPosition() == null ? null : interview.getJobPosition().getCompany().getName())
                .positionName(interview.getJobPosition() == null ? null : interview.getJobPosition().getName())
                .questionAnswers(questionAnswers)
                .feedback(toFeedbackResponse(feedback))
                .build();
    }

    private InterviewFeedbackRequest prepareFeedbackRequest(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findWithUserById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        validateFeedbackGeneration(userId, interview);

        List<InterviewQuestion> questions = interviewQuestionExecutionOrderResolver.resolve(
                interviewQuestionRepository.findAllByInterviewIdWithParentOrderByQuestionOrderAsc(interviewId));
        List<InterviewAnswer> answers = interviewAnswerRepository.findAllByInterviewIdWithQuestion(interviewId);
        validateQuestionAnswers(questions, answers);

        Map<Long, InterviewAnswer> answerByQuestionId = answers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getInterviewQuestion().getId(),
                        Function.identity()
                ));

        List<InterviewFeedbackRequest.QuestionAnswer> questionAnswers = questions.stream()
                .map(question -> InterviewFeedbackRequest.QuestionAnswer.builder()
                        .questionOrder(question.getQuestionOrder())
                        .questionContent(question.getContent())
                        .answerContent(answerByQuestionId.get(question.getId()).getAnswerContent())
                        .build())
                .toList();

        return InterviewFeedbackRequest.builder()
                .interviewTitle(interview.getTitle())
                .questionAnswers(questionAnswers)
                .build();
    }

    private void validateFeedbackGeneration(Long userId, Interview interview) {
        validateCompletedInterviewOwner(userId, interview);

        if (feedbackRepository.existsByInterviewId(interview.getId())) {
            throw new BusinessException(ErrorCode.FEEDBACK_ALREADY_EXISTS);
        }
    }

    private void validateCompletedInterviewOwner(Long userId, Interview interview) {
        if (!interview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_COMPLETED);
        }
    }

    private void validateQuestionAnswers(List<InterviewQuestion> questions, List<InterviewAnswer> answers) {
        if (questions.isEmpty() || answers.size() != questions.size()) {
            throw new BusinessException(ErrorCode.FEEDBACK_GENERATION_NOT_AVAILABLE);
        }
    }

    private Feedback saveFeedback(Long interviewId, InterviewFeedbackResult result) {
        try {
            Interview interview = interviewRepository.getReferenceById(interviewId);
            return feedbackRepository.saveAndFlush(Feedback.builder()
                    .interview(interview)
                    .overallScore(result.getOverallScore())
                    .strengths(result.getStrengths())
                    .weaknesses(result.getWeaknesses())
                    .improvementSuggestions(result.getImprovementSuggestions())
                    .summary(result.getSummary())
                    .aiModel(result.getAiModel())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.FEEDBACK_ALREADY_EXISTS);
        }
    }

    private InterviewResultQuestionAnswerResponse toQuestionAnswerResponse(InterviewQuestion question,
                                                                            InterviewAnswer answer,
                                                                            QuestionEvaluation evaluation) {
        return InterviewResultQuestionAnswerResponse.builder()
                .questionId(question.getId())
                .parentQuestionId(question.getParentQuestion() == null ? null : question.getParentQuestion().getId())
                .questionOrder(question.getQuestionOrder())
                .questionContent(question.getContent())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .followUp(question.getParentQuestion() != null)
                .answerContent(answer.getAnswerContent())
                .answeredAt(answer.getAnsweredAt())
                .evaluation(evaluation == null ? null : toQuestionEvaluationResponse(evaluation))
                .build();
    }

    private InterviewResultQuestionEvaluationResponse toQuestionEvaluationResponse(QuestionEvaluation evaluation) {
        return InterviewResultQuestionEvaluationResponse.builder()
                .evaluationId(evaluation.getId())
                .score(evaluation.getScore())
                .strengths(evaluation.getStrengths())
                .weaknesses(evaluation.getWeaknesses())
                .improvementSuggestion(evaluation.getImprovementSuggestion())
                .reasoning(evaluation.getReasoning())
                .build();
    }

    private InterviewResultFeedbackResponse toFeedbackResponse(Feedback feedback) {
        return InterviewResultFeedbackResponse.builder()
                .overallScore(feedback.getOverallScore())
                .strengths(feedback.getStrengths())
                .weaknesses(feedback.getWeaknesses())
                .improvementSuggestions(feedback.getImprovementSuggestions())
                .summary(feedback.getSummary())
                .build();
    }
}
