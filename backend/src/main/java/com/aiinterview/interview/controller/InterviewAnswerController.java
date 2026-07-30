package com.aiinterview.interview.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.service.InterviewAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews/questions")
@RequiredArgsConstructor
public class InterviewAnswerController {

    private final InterviewAnswerService interviewAnswerService;

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<ApiResponse<InterviewAnswerCreateResponse>> createAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId,
            @RequestBody @Valid InterviewAnswerCreateRequest request) {

        InterviewAnswerCreateResponse response = interviewAnswerService.createAnswer(
                userDetails.getId(), questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.CREATED, response));
    }
}
