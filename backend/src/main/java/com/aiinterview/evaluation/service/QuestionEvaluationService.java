package com.aiinterview.evaluation.service;

import com.aiinterview.evaluation.dto.QuestionEvaluationResponse;

import java.util.List;

public interface QuestionEvaluationService {

    QuestionEvaluationResponse evaluate(Long userId, Long answerId);

    void evaluateMissing(Long userId, List<Long> answerIds);
}
