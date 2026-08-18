package com.aiinterview.feedback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewResultQuestionEvaluationResponse {

    private final Long evaluationId;
    private final int score;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestion;
    private final String reasoning;

    @Builder
    public InterviewResultQuestionEvaluationResponse(Long evaluationId, int score, String strengths,
                                                     String weaknesses, String improvementSuggestion,
                                                     String reasoning) {
        this.evaluationId = evaluationId;
        this.score = score;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestion = improvementSuggestion;
        this.reasoning = reasoning;
    }
}
