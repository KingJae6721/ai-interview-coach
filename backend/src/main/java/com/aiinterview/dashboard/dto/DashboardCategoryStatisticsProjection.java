package com.aiinterview.dashboard.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;

public class DashboardCategoryStatisticsProjection {

    private final InterviewQuestionCategory category;
    private final long interviewCount;
    private final long evaluationCount;
    private final Double averageScore;

    public DashboardCategoryStatisticsProjection(InterviewQuestionCategory category, Long interviewCount,
                                                 Long evaluationCount, Double averageScore) {
        this.category = category;
        this.interviewCount = interviewCount;
        this.evaluationCount = evaluationCount;
        this.averageScore = averageScore;
    }

    public InterviewQuestionCategory getCategory() {
        return category;
    }

    public long getInterviewCount() {
        return interviewCount;
    }

    public long getEvaluationCount() {
        return evaluationCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }
}
