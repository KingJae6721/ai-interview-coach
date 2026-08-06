package com.aiinterview.feedback.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class InterviewResultResponse {

    private final Long interviewId;
    private final String title;
    private final InterviewStatus status;
    private final List<InterviewResultQuestionAnswerResponse> questionAnswers;
    private final InterviewResultFeedbackResponse feedback;

    @Builder
    public InterviewResultResponse(Long interviewId, String title, InterviewStatus status,
                                   List<InterviewResultQuestionAnswerResponse> questionAnswers,
                                   InterviewResultFeedbackResponse feedback) {
        this.interviewId = interviewId;
        this.title = title;
        this.status = status;
        this.questionAnswers = questionAnswers;
        this.feedback = feedback;
    }
}
