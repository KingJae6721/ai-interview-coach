package com.aiinterview.jobposting.service;

import com.aiinterview.ai.dto.JobPostingAnalysisResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeRequest;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeResponse;
import com.aiinterview.jobposting.fetch.FetchedJobPostingContent;
import com.aiinterview.jobposting.fetch.JobPostingContentFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPositionRepository jobPositionRepository;
    private final JobPostingContentFetcher jobPostingContentFetcher;
    private final AiService aiService;
    private final JobPostingPersistenceService jobPostingPersistenceService;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public JobPostingAnalyzeResponse analyzeJobPosting(JobPostingAnalyzeRequest request) {
        if (!jobPositionRepository.existsById(request.getJobPositionId())) {
            throw new BusinessException(ErrorCode.JOB_POSITION_NOT_FOUND);
        }

        FetchedJobPostingContent fetchedContent = jobPostingContentFetcher.fetch(request.getPostingUrl());
        JobPostingAnalysisResult analysisResult = aiService.analyzeJobPosting(fetchedContent.content());

        return jobPostingPersistenceService.save(request.getJobPositionId(), request.getPostingUrl(), fetchedContent,
                analysisResult);
    }
}
