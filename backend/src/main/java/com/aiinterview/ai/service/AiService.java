package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.InterviewFeedbackResult;
import com.aiinterview.ai.dto.QuestionEvaluationRequest;
import com.aiinterview.ai.dto.QuestionEvaluationResult;

import java.util.List;
import java.util.Optional;

public interface AiService {

    List<String> generateInterviewQuestions(String questionGenerationPrompt);

    Optional<String> generateFollowUpQuestion(String answerContent);

    InterviewFeedbackResult generateInterviewFeedback(InterviewFeedbackRequest request);

    QuestionEvaluationResult evaluateQuestionAnswer(QuestionEvaluationRequest request);
}
