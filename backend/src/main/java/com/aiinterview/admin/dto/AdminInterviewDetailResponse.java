package com.aiinterview.admin.dto;

import com.aiinterview.interview.entity.InterviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminInterviewDetailResponse(
        Long interviewId,
        String title,
        InterviewStatus status,
        Long userId,
        String userEmail,
        Long jobPostingId,
        String jobPostingTitle,
        String jobPostingSummary,
        boolean resumeUsed,
        Long resumeId,
        List<AdminInterviewQuestionResponse> questions,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt
) {
}
