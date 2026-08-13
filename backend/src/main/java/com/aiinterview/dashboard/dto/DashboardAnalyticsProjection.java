package com.aiinterview.dashboard.dto;

import java.time.LocalDateTime;

public class DashboardAnalyticsProjection {

    private final LocalDateTime periodStartAt;
    private final Double averageScore;
    private final long interviewCount;

    public DashboardAnalyticsProjection(LocalDateTime periodStartAt, Double averageScore, Long interviewCount) {
        this.periodStartAt = periodStartAt;
        this.averageScore = averageScore;
        this.interviewCount = interviewCount;
    }

    public LocalDateTime getPeriodStartAt() {
        return periodStartAt;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public long getInterviewCount() {
        return interviewCount;
    }
}
