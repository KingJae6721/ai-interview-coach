package com.aiinterview.interview.service;

import com.aiinterview.interview.dto.InterviewCreateResponse;

public interface InterviewService {

    InterviewCreateResponse createInterview(Long userId);
}
