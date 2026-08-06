package com.aiinterview.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class InterviewFeedbackRequest {

    private final String interviewTitle;
    private final List<QuestionAnswer> questionAnswers;

    @Builder
    public InterviewFeedbackRequest(String interviewTitle, List<QuestionAnswer> questionAnswers) {
        this.interviewTitle = interviewTitle;
        this.questionAnswers = questionAnswers;
    }

    @Getter
    @Builder
    public static class QuestionAnswer {
        private final Integer questionOrder;
        private final String questionContent;
        private final String answerContent;

        public QuestionAnswer(Integer questionOrder, String questionContent, String answerContent) {
            this.questionOrder = questionOrder;
            this.questionContent = questionContent;
            this.answerContent = answerContent;
        }
    }
}
