package com.aiinterview.feedback.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewResultQuestionAnswerResponse {

    private final Long questionId;
    private final Long parentQuestionId;
    private final Integer questionOrder;
    private final String questionContent;
    private final InterviewQuestionCategory category;
    private final InterviewQuestionDifficulty difficulty;
    private final boolean followUp;
    private final String answerContent;
    private final LocalDateTime answeredAt;
    private final InterviewResultQuestionEvaluationResponse evaluation;

    @Builder
    public InterviewResultQuestionAnswerResponse(Long questionId, Long parentQuestionId, Integer questionOrder,
                                                 String questionContent, InterviewQuestionCategory category,
                                                 InterviewQuestionDifficulty difficulty, boolean followUp,
                                                 String answerContent, LocalDateTime answeredAt,
                                                 InterviewResultQuestionEvaluationResponse evaluation) {
        this.questionId = questionId;
        this.parentQuestionId = parentQuestionId;
        this.questionOrder = questionOrder;
        this.questionContent = questionContent;
        this.category = category;
        this.difficulty = difficulty;
        this.followUp = followUp;
        this.answerContent = answerContent;
        this.answeredAt = answeredAt;
        this.evaluation = evaluation;
    }
}
