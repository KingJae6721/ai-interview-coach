package com.aiinterview.evaluation.service;

import com.aiinterview.ai.dto.QuestionEvaluationRequest;
import com.aiinterview.ai.dto.QuestionEvaluationResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.evaluation.dto.QuestionEvaluationResponse;
import com.aiinterview.evaluation.entity.QuestionEvaluation;
import com.aiinterview.evaluation.repository.QuestionEvaluationRepository;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionEvaluationServiceImpl implements QuestionEvaluationService {

    private final InterviewAnswerRepository interviewAnswerRepository;
    private final QuestionEvaluationRepository questionEvaluationRepository;
    private final QuestionEvaluationPersistenceService questionEvaluationPersistenceService;
    private final AiService aiService;

    @Override
    public QuestionEvaluationResponse evaluate(Long userId, Long answerId) {
        QuestionEvaluationRequest request = prepareEvaluationRequest(userId, answerId);
        QuestionEvaluationResult result = aiService.evaluateQuestionAnswer(request);
        QuestionEvaluation evaluation = questionEvaluationPersistenceService.save(answerId, result);

        return toResponse(evaluation);
    }

    @Override
    public void evaluateMissing(Long userId, List<Long> answerIds) {
        if (answerIds.isEmpty()) {
            return;
        }

        Set<Long> evaluatedAnswerIds = questionEvaluationRepository
                .findEvaluatedAnswerIdsByAnswerIdIn(answerIds);
        List<Long> missingAnswerIds = answerIds.stream()
                .filter(answerId -> !evaluatedAnswerIds.contains(answerId))
                .toList();
        if (missingAnswerIds.isEmpty()) {
            return;
        }

        Map<Long, InterviewAnswer> answerById = interviewAnswerRepository
                .findAllWithQuestionInterviewAndUserByIdIn(missingAnswerIds).stream()
                .collect(Collectors.toMap(InterviewAnswer::getId, Function.identity()));

        for (Long answerId : missingAnswerIds) {
            InterviewAnswer answer = getOwnedAnswer(userId, answerId, answerById);
            QuestionEvaluationResult result = aiService.evaluateQuestionAnswer(toEvaluationRequest(answer));
            saveMissingEvaluation(answerId, result);
        }
    }

    private QuestionEvaluationRequest prepareEvaluationRequest(Long userId, Long answerId) {
        InterviewAnswer answer = interviewAnswerRepository.findWithQuestionInterviewAndUserById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_ANSWER_NOT_FOUND));

        validateOwner(userId, answer);
        if (questionEvaluationRepository.existsByAnswerId(answerId)) {
            throw new BusinessException(ErrorCode.QUESTION_EVALUATION_ALREADY_EXISTS);
        }

        return toEvaluationRequest(answer);
    }

    private InterviewAnswer getOwnedAnswer(Long userId, Long answerId, Map<Long, InterviewAnswer> answerById) {
        InterviewAnswer answer = answerById.get(answerId);
        if (answer == null) {
            throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_NOT_FOUND);
        }
        validateOwner(userId, answer);
        return answer;
    }

    private void validateOwner(Long userId, InterviewAnswer answer) {
        if (!answer.getInterviewQuestion().getInterview().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private QuestionEvaluationRequest toEvaluationRequest(InterviewAnswer answer) {
        InterviewQuestion question = answer.getInterviewQuestion();
        return QuestionEvaluationRequest.builder()
                .questionContent(question.getContent())
                .answerContent(answer.getAnswerContent())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .build();
    }

    private void saveMissingEvaluation(Long answerId, QuestionEvaluationResult result) {
        try {
            questionEvaluationPersistenceService.save(answerId, result);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() != ErrorCode.QUESTION_EVALUATION_ALREADY_EXISTS) {
                throw exception;
            }
        }
    }

    private QuestionEvaluationResponse toResponse(QuestionEvaluation evaluation) {
        return QuestionEvaluationResponse.builder()
                .evaluationId(evaluation.getId())
                .answerId(evaluation.getAnswer().getId())
                .score(evaluation.getScore())
                .strengths(evaluation.getStrengths())
                .weaknesses(evaluation.getWeaknesses())
                .improvementSuggestion(evaluation.getImprovementSuggestion())
                .reasoning(evaluation.getReasoning())
                .build();
    }
}
