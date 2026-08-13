package com.aiinterview.evaluation.service;

import com.aiinterview.evaluation.dto.QuestionEvaluationResponse;

public interface QuestionEvaluationService {

    QuestionEvaluationResponse evaluate(Long userId, Long answerId);
}
