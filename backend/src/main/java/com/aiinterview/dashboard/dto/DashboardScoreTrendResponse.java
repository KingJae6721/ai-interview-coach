package com.aiinterview.dashboard.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DashboardScoreTrendResponse {

    private final Long interviewId;
    private final String title;
    private final LocalDateTime completedAt;
    private final int overallScore;

    public DashboardScoreTrendResponse(Long interviewId, String title, LocalDateTime completedAt, int overallScore) {
        this.interviewId = interviewId;
        this.title = title;
        this.completedAt = completedAt;
        this.overallScore = overallScore;
    }
}
