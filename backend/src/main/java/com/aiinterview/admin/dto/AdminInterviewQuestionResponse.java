package com.aiinterview.admin.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import com.aiinterview.interview.entity.InterviewQuestionType;

public record AdminInterviewQuestionResponse(
        Long questionId,
        Long parentQuestionId,
        Integer questionOrder,
        String questionContent,
        InterviewQuestionCategory category,
        InterviewQuestionDifficulty difficulty,
        InterviewQuestionType type,
        Boolean answerExists,
        Boolean evaluationExists
) {
}
