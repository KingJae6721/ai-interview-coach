package com.aiinterview.interview.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.dto.InterviewCompleteResponse;
import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.dto.InterviewQuestionResponse;
import jakarta.validation.Valid;
import com.aiinterview.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewCreateResponse>> createInterview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid InterviewCreateRequest request) {

        InterviewCreateResponse response = interviewService.createInterview(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.INTERVIEW_CREATED, response));
    }

    @GetMapping("/{interviewId}/questions")
    public ResponseEntity<ApiResponse<List<InterviewQuestionResponse>>> getInterviewQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interviewId) {

        List<InterviewQuestionResponse> response = interviewService.getInterviewQuestions(
                userDetails.getId(), interviewId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<InterviewAnswerCreateResponse>> submitAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId,
            @RequestBody @Valid InterviewAnswerCreateRequest request) {

        InterviewAnswerCreateResponse response = interviewService.submitAnswer(
                userDetails.getId(), questionId, request);

        HttpStatus status = response.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ApiResponse.success(ResultCode.SUCCESS, response));
    }

    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<ApiResponse<InterviewCompleteResponse>> completeInterview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interviewId) {

        InterviewCompleteResponse response = interviewService.completeInterview(userDetails.getId(), interviewId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, response));
    }
}
