package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class InterviewProgressResponse {

    private final Long interviewId;
    private final InterviewStatus status;
    private final List<InterviewProgressQuestionResponse> questions;
    private final Long nextQuestionId;
    private final boolean allAnswered;

    @Builder
    public InterviewProgressResponse(Long interviewId, InterviewStatus status,
                                     List<InterviewProgressQuestionResponse> questions,
                                     Long nextQuestionId, boolean allAnswered) {
        this.interviewId = interviewId;
        this.status = status;
        this.questions = questions;
        this.nextQuestionId = nextQuestionId;
        this.allAnswered = allAnswered;
    }
}
