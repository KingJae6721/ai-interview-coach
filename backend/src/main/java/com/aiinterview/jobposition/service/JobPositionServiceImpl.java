package com.aiinterview.jobposition.service;

import com.aiinterview.jobposition.dto.JobPositionResponse;
import com.aiinterview.jobposition.mapper.JobPositionMapper;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPositionServiceImpl implements JobPositionService {

    private final JobPositionRepository jobPositionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<JobPositionResponse> getJobPositions() {
        return jobPositionRepository.findAllWithCompany().stream()
                .map(JobPositionMapper::toResponse)
                .toList();
    }
}
