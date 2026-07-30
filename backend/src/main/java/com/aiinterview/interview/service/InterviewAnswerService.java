package com.aiinterview.interview.service;

import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;

public interface InterviewAnswerService {

    InterviewAnswerCreateResponse createAnswer(Long userId, Long questionId, InterviewAnswerCreateRequest request);
}
