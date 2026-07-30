package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewCompleteResponse {

    private final Long interviewId;
    private final InterviewStatus status;
    private final LocalDateTime completedAt;

    @Builder
    public InterviewCompleteResponse(Long interviewId, InterviewStatus status, LocalDateTime completedAt) {
        this.interviewId = interviewId;
        this.status = status;
        this.completedAt = completedAt;
    }
}
