package com.aiinterview.ai.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;

public record InterviewQuestionDistribution(
        int questionOrder,
        InterviewQuestionDifficulty difficulty,
        InterviewQuestionCategory category
) {
}
