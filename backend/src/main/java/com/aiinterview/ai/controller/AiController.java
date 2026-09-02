package com.aiinterview.ai.controller;

import com.aiinterview.ai.dto.InterviewFollowUpQuestionResponse;
import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final InterviewService interviewService;

    @PostMapping("/questions/{questionId}/follow-up")
    public ResponseEntity<ApiResponse<InterviewFollowUpQuestionResponse>> generateFollowUpQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("questionId") Long questionId) {

        InterviewFollowUpQuestionResponse response = interviewService.generateFollowUpQuestion(
                userDetails.getId(), questionId);

        HttpStatus status = response.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ApiResponse.success(ResultCode.SUCCESS, response));
    }
}
