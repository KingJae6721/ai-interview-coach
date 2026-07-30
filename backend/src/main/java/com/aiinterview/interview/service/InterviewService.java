package com.aiinterview.interview.service;

import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;

public interface InterviewService {

    InterviewCreateResponse createInterview(Long userId, InterviewCreateRequest request);
}
