package com.aiinterview.dashboard.dto;

import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DashboardDifficultyStatisticsResponse {

    private final InterviewQuestionDifficulty difficulty;
    private final long interviewCount;
    private final long questionCount;
    private final long evaluationCount;
    private final Double averageScore;

    @Builder
    public DashboardDifficultyStatisticsResponse(InterviewQuestionDifficulty difficulty, long interviewCount,
                                                 long questionCount, long evaluationCount, Double averageScore) {
        this.difficulty = difficulty;
        this.interviewCount = interviewCount;
        this.questionCount = questionCount;
        this.evaluationCount = evaluationCount;
        this.averageScore = averageScore;
    }
}
