package com.aiinterview.jobposting.controller;

import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeRequest;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeResponse;
import com.aiinterview.jobposting.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<JobPostingAnalyzeResponse>> analyzeJobPosting(
            @RequestBody @Valid JobPostingAnalyzeRequest request) {
        JobPostingAnalyzeResponse response = jobPostingService.analyzeJobPosting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.CREATED, response));
    }
}
