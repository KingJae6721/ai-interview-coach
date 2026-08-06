package com.aiinterview.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewFeedbackResult {

    private final int overallScore;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestions;
    private final String summary;
    private final String aiModel;

    @Builder
    public InterviewFeedbackResult(int overallScore, String strengths, String weaknesses,
                                   String improvementSuggestions, String summary, String aiModel) {
        this.overallScore = overallScore;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestions = improvementSuggestions;
        this.summary = summary;
        this.aiModel = aiModel;
    }
}
