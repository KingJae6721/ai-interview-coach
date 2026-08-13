package com.aiinterview.evaluation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionEvaluationResponse {

    private final Long evaluationId;
    private final Long answerId;
    private final int score;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestion;
    private final String reasoning;

    @Builder
    public QuestionEvaluationResponse(Long evaluationId, Long answerId, int score, String strengths,
                                      String weaknesses, String improvementSuggestion, String reasoning) {
        this.evaluationId = evaluationId;
        this.answerId = answerId;
        this.score = score;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestion = improvementSuggestion;
        this.reasoning = reasoning;
    }
}
