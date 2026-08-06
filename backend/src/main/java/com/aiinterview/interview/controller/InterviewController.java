package com.aiinterview.interview.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.feedback.dto.FeedbackGenerateResponse;
import com.aiinterview.feedback.dto.InterviewResultResponse;
import com.aiinterview.feedback.service.FeedbackService;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.dto.InterviewCompleteResponse;
import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.dto.InterviewQuestionResponse;
import com.aiinterview.interview.dto.InterviewProgressResponse;
import com.aiinterview.interview.dto.InterviewHistoryResponse;
import jakarta.validation.Valid;
import com.aiinterview.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewCreateResponse>> createInterview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid InterviewCreateRequest request) {

        InterviewCreateResponse response = interviewService.createInterview(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.INTERVIEW_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InterviewHistoryResponse>>> getInterviewHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<InterviewHistoryResponse> response = interviewService.getInterviewHistory(userDetails.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{interviewId}/questions")
    public ResponseEntity<ApiResponse<List<InterviewQuestionResponse>>> getInterviewQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interviewId) {

        List<InterviewQuestionResponse> response = interviewService.getInterviewQuestions(
                userDetails.getId(), interviewId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{interviewId}/progress")
    public ResponseEntity<ApiResponse<InterviewProgressResponse>> getInterviewProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interviewId) {

        InterviewProgressResponse response = interviewService.getInterviewProgress(userDetails.getId(), interviewId);

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

    @PostMapping("/{interviewId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackGenerateResponse>> generateFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interviewId) {

        FeedbackGenerateResponse response = feedbackService.generateFeedback(userDetails.getId(), interviewId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.AI_FEEDBACK_COMPLETED, response));
    }

    @GetMapping("/{interviewId}/result")
    public ResponseEntity<ApiResponse<InterviewResultResponse>> getInterviewResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interviewId) {

        InterviewResultResponse response = feedbackService.getInterviewResult(userDetails.getId(), interviewId);

        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, response));
    }
}
