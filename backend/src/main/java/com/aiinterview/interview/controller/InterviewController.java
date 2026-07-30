package com.aiinterview.interview.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewCreateResponse>> createInterview(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        InterviewCreateResponse response = interviewService.createInterview(userDetails.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.INTERVIEW_CREATED, response));
    }
}
