package com.aiinterview.dashboard.dto;

import com.aiinterview.interview.entity.InterviewQuestionDifficulty;

public class DashboardDifficultyStatisticsProjection {

    private final InterviewQuestionDifficulty difficulty;
    private final long interviewCount;
    private final long evaluationCount;
    private final Double averageScore;

    public DashboardDifficultyStatisticsProjection(InterviewQuestionDifficulty difficulty, Long interviewCount,
                                                   Long evaluationCount, Double averageScore) {
        this.difficulty = difficulty;
        this.interviewCount = interviewCount;
        this.evaluationCount = evaluationCount;
        this.averageScore = averageScore;
    }

    public InterviewQuestionDifficulty getDifficulty() {
        return difficulty;
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
