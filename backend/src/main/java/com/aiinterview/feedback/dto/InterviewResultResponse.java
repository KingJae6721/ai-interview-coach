package com.aiinterview.feedback.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.time.LocalDateTime;

@Getter
public class InterviewResultResponse {

    private final Long interviewId;
    private final String title;
    private final InterviewStatus status;
    private final LocalDateTime completedAt;
    private final String companyName;
    private final String positionName;
    private final List<InterviewResultQuestionAnswerResponse> questionAnswers;
    private final InterviewResultFeedbackResponse feedback;

    @Builder
    public InterviewResultResponse(Long interviewId, String title, InterviewStatus status, LocalDateTime completedAt,
                                   String companyName, String positionName,
                                   List<InterviewResultQuestionAnswerResponse> questionAnswers,
                                   InterviewResultFeedbackResponse feedback) {
        this.interviewId = interviewId;
        this.title = title;
        this.status = status;
        this.completedAt = completedAt;
        this.companyName = companyName;
        this.positionName = positionName;
        this.questionAnswers = questionAnswers;
        this.feedback = feedback;
    }
}
