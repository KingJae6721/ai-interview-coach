package com.aiinterview.dashboard.dto;

import java.time.LocalDateTime;

public class DashboardStatisticsProjection {

    private final long totalInterviews;
    private final long completedInterviews;
    private final Double averageScore;
    private final Integer highestScore;
    private final LocalDateTime latestInterviewAt;

    public DashboardStatisticsProjection(Long totalInterviews, Long completedInterviews, Double averageScore,
                                         Integer highestScore, LocalDateTime latestInterviewAt) {
        this.totalInterviews = totalInterviews;
        this.completedInterviews = completedInterviews;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.latestInterviewAt = latestInterviewAt;
    }

    public long getTotalInterviews() {
        return totalInterviews;
    }

    public long getCompletedInterviews() {
        return completedInterviews;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public Integer getHighestScore() {
        return highestScore;
    }

    public LocalDateTime getLatestInterviewAt() {
        return latestInterviewAt;
    }
}
