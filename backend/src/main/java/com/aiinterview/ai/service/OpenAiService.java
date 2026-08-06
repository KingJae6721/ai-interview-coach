package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.InterviewFeedbackResult;

import java.util.List;
import java.util.Optional;

public interface OpenAiService {

    List<String> generateInterviewQuestions(String interviewTitle);

    Optional<String> generateFollowUpQuestion(String answerContent);

    InterviewFeedbackResult generateInterviewFeedback(InterviewFeedbackRequest request);
}
