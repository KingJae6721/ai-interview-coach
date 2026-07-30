package com.aiinterview.interview.service;

import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.dto.InterviewCompleteResponse;
import com.aiinterview.interview.dto.InterviewQuestionResponse;

import java.util.List;

public interface InterviewService {

    InterviewCreateResponse createInterview(Long userId, InterviewCreateRequest request);

    List<InterviewQuestionResponse> getInterviewQuestions(Long userId, Long interviewId);

    InterviewAnswerCreateResponse submitAnswer(Long userId, Long questionId, InterviewAnswerCreateRequest request);

    InterviewCompleteResponse completeInterview(Long userId, Long interviewId);
}
