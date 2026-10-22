package com.aiinterview.admin.dto;

import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewStatus;

import java.time.LocalDateTime;

public record AdminLatestInterviewResponse(
        Long interviewId,
        String title,
        InterviewStatus status,
        LocalDateTime createdAt
) {
    public static AdminLatestInterviewResponse from(Interview interview) {
        return new AdminLatestInterviewResponse(
                interview.getId(), interview.getTitle(), interview.getStatus(), interview.getCreatedAt());
    }
}
