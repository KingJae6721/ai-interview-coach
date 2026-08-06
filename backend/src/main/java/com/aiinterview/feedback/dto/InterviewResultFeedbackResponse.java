package com.aiinterview.feedback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewResultFeedbackResponse {

    private final int overallScore;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestions;
    private final String summary;

    @Builder
    public InterviewResultFeedbackResponse(int overallScore, String strengths, String weaknesses,
                                           String improvementSuggestions, String summary) {
        this.overallScore = overallScore;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestions = improvementSuggestions;
        this.summary = summary;
    }
}
