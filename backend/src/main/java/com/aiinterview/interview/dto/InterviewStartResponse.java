package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewStartResponse {

    private final Long interviewId;
    private final InterviewStatus status;
    private final LocalDateTime startedAt;

    @Builder
    public InterviewStartResponse(Long interviewId, InterviewStatus status, LocalDateTime startedAt) {
        this.interviewId = interviewId;
        this.status = status;
        this.startedAt = startedAt;
    }
}
