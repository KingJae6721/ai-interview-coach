package com.aiinterview.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionEvaluationResult {

    private final int score;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestion;
    private final String reasoning;
    private final String aiModel;

    @Builder
    public QuestionEvaluationResult(int score, String strengths, String weaknesses,
                                    String improvementSuggestion, String reasoning, String aiModel) {
        this.score = score;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestion = improvementSuggestion;
        this.reasoning = reasoning;
        this.aiModel = aiModel;
    }
}
