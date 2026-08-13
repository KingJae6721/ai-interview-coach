package com.aiinterview.jobposition.mapper;

import com.aiinterview.company.entity.Company;
import com.aiinterview.jobposition.dto.JobPositionResponse;
import com.aiinterview.jobposition.entity.JobPosition;

public final class JobPositionMapper {

    private JobPositionMapper() {
    }

    public static JobPositionResponse toResponse(JobPosition jobPosition) {
        Company company = jobPosition.getCompany();

        return JobPositionResponse.builder()
                .jobPositionId(jobPosition.getId())
                .positionName(jobPosition.getName())
                .companyId(company.getId())
                .companyName(company.getName())
                .techStack(jobPosition.getTechStack())
                .build();
    }
}
