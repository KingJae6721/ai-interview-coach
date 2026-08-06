package com.aiinterview.feedback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FeedbackGenerateResponse {

    private final Long feedbackId;
    private final Long interviewId;
    private final int overallScore;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestions;
    private final String summary;

    @Builder
    public FeedbackGenerateResponse(Long feedbackId, Long interviewId, int overallScore, String strengths,
                                    String weaknesses, String improvementSuggestions, String summary) {
        this.feedbackId = feedbackId;
        this.interviewId = interviewId;
        this.overallScore = overallScore;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestions = improvementSuggestions;
        this.summary = summary;
    }
}
