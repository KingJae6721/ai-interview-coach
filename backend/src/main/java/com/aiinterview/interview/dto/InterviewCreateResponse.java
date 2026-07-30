package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewCreateResponse {

    private final Long interviewId;
    private final String title;
    private final InterviewStatus status;
    private final int questionCount;

    @Builder
    public InterviewCreateResponse(Long interviewId, String title, InterviewStatus status, int questionCount) {
        this.interviewId = interviewId;
        this.title = title;
        this.status = status;
        this.questionCount = questionCount;
    }
}
