package com.aiinterview.evaluation.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.evaluation.dto.QuestionEvaluationResponse;
import com.aiinterview.evaluation.service.QuestionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class QuestionEvaluationController {

    private final QuestionEvaluationService questionEvaluationService;

    @PostMapping("/{answerId}/evaluation")
    public ResponseEntity<ApiResponse<QuestionEvaluationResponse>> evaluate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("answerId") Long answerId) {

        QuestionEvaluationResponse response = questionEvaluationService.evaluate(userDetails.getId(), answerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.SUCCESS, response));
    }
}
