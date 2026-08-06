package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.InterviewFeedbackResult;

import java.util.List;

public interface OpenAiService {

    List<String> generateInterviewQuestions(String interviewTitle);

    InterviewFeedbackResult generateInterviewFeedback(InterviewFeedbackRequest request);
}
