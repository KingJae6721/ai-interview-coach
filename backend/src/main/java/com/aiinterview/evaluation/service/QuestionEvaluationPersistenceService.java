package com.aiinterview.evaluation.service;

import com.aiinterview.ai.dto.QuestionEvaluationResult;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.evaluation.entity.QuestionEvaluation;
import com.aiinterview.evaluation.repository.QuestionEvaluationRepository;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionEvaluationPersistenceService {

    private final InterviewAnswerRepository interviewAnswerRepository;
    private final QuestionEvaluationRepository questionEvaluationRepository;

    @Transactional
    public QuestionEvaluation save(Long answerId, QuestionEvaluationResult result) {
        if (questionEvaluationRepository.existsByAnswerId(answerId)) {
            throw new BusinessException(ErrorCode.QUESTION_EVALUATION_ALREADY_EXISTS);
        }

        try {
            InterviewAnswer answer = interviewAnswerRepository.getReferenceById(answerId);
            return questionEvaluationRepository.saveAndFlush(QuestionEvaluation.builder()
                    .answer(answer)
                    .score(result.getScore())
                    .strengths(result.getStrengths())
                    .weaknesses(result.getWeaknesses())
                    .improvementSuggestion(result.getImprovementSuggestion())
                    .reasoning(result.getReasoning())
                    .aiModel(result.getAiModel())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.QUESTION_EVALUATION_ALREADY_EXISTS);
        }
    }
}
