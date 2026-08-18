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

    private QuestionEvaluationRequest prepareEvaluationRequest(Long userId, Long answerId) {
        InterviewAnswer answer = interviewAnswerRepository.findWithQuestionInterviewAndUserById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_ANSWER_NOT_FOUND));

        if (!answer.getInterviewQuestion().getInterview().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (questionEvaluationRepository.existsByAnswerId(answerId)) {
            throw new BusinessException(ErrorCode.QUESTION_EVALUATION_ALREADY_EXISTS);
        }

        InterviewQuestion question = answer.getInterviewQuestion();
        return QuestionEvaluationRequest.builder()
                .questionContent(question.getContent())
                .answerContent(answer.getAnswerContent())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .build();
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
