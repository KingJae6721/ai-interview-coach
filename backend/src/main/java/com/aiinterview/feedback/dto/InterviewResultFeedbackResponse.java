package com.aiinterview.feedback.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewResultFeedbackResponse {

    private final Integer overallScore;
    private final boolean partial;
    private final int answeredCount;
    private final int totalQuestionCount;
    private final String strengths;
    private final String weaknesses;
    private final String improvementSuggestions;
    private final String summary;

    @Builder
    public InterviewResultFeedbackResponse(Integer overallScore, boolean partial, int answeredCount,
                                           int totalQuestionCount, String strengths, String weaknesses,
                                           String improvementSuggestions, String summary) {
        this.overallScore = overallScore;
        this.partial = partial;
        this.answeredCount = answeredCount;
        this.totalQuestionCount = totalQuestionCount;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestions = improvementSuggestions;
        this.summary = summary;
    }
}
