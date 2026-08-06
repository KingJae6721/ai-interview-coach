package com.aiinterview.feedback.service;

import com.aiinterview.feedback.dto.FeedbackGenerateResponse;
import com.aiinterview.feedback.dto.InterviewResultResponse;

public interface FeedbackService {

    FeedbackGenerateResponse generateFeedback(Long userId, Long interviewId);

    InterviewResultResponse getInterviewResult(Long userId, Long interviewId);
}
