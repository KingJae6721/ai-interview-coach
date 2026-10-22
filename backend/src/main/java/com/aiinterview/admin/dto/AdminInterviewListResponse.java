package com.aiinterview.admin.dto;

import com.aiinterview.interview.entity.InterviewStatus;

import java.time.LocalDateTime;

public record AdminInterviewListResponse(
        Long interviewId,
        String title,
        Long userId,
        String userEmail,
        InterviewStatus status,
        String jobPostingSummary,
        Long questionCount,
        Long answeredQuestionCount,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
