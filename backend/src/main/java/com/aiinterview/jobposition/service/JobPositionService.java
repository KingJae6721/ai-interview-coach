package com.aiinterview.jobposition.service;

import com.aiinterview.jobposition.dto.JobPositionResponse;

import java.util.List;

public interface JobPositionService {

    List<JobPositionResponse> getJobPositions();
}
