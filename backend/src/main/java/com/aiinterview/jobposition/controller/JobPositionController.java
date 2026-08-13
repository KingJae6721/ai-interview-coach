package com.aiinterview.jobposition.controller;

import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.jobposition.dto.JobPositionResponse;
import com.aiinterview.jobposition.service.JobPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/job-positions")
@RequiredArgsConstructor
public class JobPositionController {

    private final JobPositionService jobPositionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobPositionResponse>>> getJobPositions() {
        List<JobPositionResponse> response = jobPositionService.getJobPositions();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
