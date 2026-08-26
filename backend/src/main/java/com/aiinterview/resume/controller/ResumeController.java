package com.aiinterview.resume.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.code.ResultCode;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.resume.dto.ResumeAnalyzeResponse;
import com.aiinterview.resume.dto.ResumeSummaryResponse;
import com.aiinterview.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeAnalyzeResponse>> analyzeResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        ResumeAnalyzeResponse response = resumeService.analyzeResume(userDetails.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(ResultCode.CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeSummaryResponse>>> getResumes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.getResumes(userDetails.getId())));
    }
}
