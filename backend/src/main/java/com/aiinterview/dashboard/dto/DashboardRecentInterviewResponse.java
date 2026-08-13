package com.aiinterview.dashboard.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DashboardRecentInterviewResponse {

    private final Long interviewId;
    private final String title;
    private final InterviewStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;
    private final String companyName;
    private final String positionName;
    private final Integer overallScore;

    public DashboardRecentInterviewResponse(Long interviewId, String title, InterviewStatus status,
                                            LocalDateTime createdAt, LocalDateTime completedAt,
                                            String companyName, String positionName, Integer overallScore) {
        this.interviewId = interviewId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.companyName = companyName;
        this.positionName = positionName;
        this.overallScore = overallScore;
    }
}
