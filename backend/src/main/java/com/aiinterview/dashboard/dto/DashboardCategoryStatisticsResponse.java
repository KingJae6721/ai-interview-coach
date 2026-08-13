package com.aiinterview.dashboard.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DashboardCategoryStatisticsResponse {

    private final InterviewQuestionCategory category;
    private final long interviewCount;
    private final long questionCount;
    private final long evaluationCount;
    private final Double averageScore;

    @Builder
    public DashboardCategoryStatisticsResponse(InterviewQuestionCategory category, long interviewCount,
                                               long questionCount, long evaluationCount, Double averageScore) {
        this.category = category;
        this.interviewCount = interviewCount;
        this.questionCount = questionCount;
        this.evaluationCount = evaluationCount;
        this.averageScore = averageScore;
    }
}
