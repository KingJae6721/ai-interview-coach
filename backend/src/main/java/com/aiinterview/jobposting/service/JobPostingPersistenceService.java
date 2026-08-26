package com.aiinterview.jobposting.service;

import com.aiinterview.ai.dto.JobPostingAnalysisResult;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeResponse;
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import com.aiinterview.jobposting.fetch.FetchedJobPostingContent;
import com.aiinterview.jobposting.repository.JobPostingAnalysisRepository;
import com.aiinterview.jobposting.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
class JobPostingPersistenceService {

    private final JobPositionRepository jobPositionRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingAnalysisRepository jobPostingAnalysisRepository;

    @Transactional
    public JobPostingAnalyzeResponse save(Long jobPositionId, String postingUrl, FetchedJobPostingContent fetchedContent,
                                          JobPostingAnalysisResult analysisResult) {
        JobPosition jobPosition = jobPositionRepository.findById(jobPositionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSITION_NOT_FOUND));

        JobPosting jobPosting = jobPostingRepository.save(JobPosting.builder()
                .jobPosition(jobPosition)
                .postingUrl(postingUrl)
                .title(fetchedContent.title())
                .extractedContent(fetchedContent.content())
                .build());
        JobPostingAnalysis analysis = jobPostingAnalysisRepository.save(JobPostingAnalysis.builder()
                .jobPosting(jobPosting)
                .companyName(analysisResult.getCompanyName())
                .positionName(analysisResult.getPositionName())
                .responsibilities(analysisResult.getResponsibilities())
                .requiredQualifications(analysisResult.getRequiredQualifications())
                .preferredQualifications(analysisResult.getPreferredQualifications())
                .techStack(analysisResult.getTechStack())
                .experienceRequirements(analysisResult.getExperienceRequirements())
                .keywords(analysisResult.getKeywords())
                .summary(analysisResult.getSummary())
                .aiModel(analysisResult.getAiModel())
                .analyzedAt(LocalDateTime.now())
                .build());

        return JobPostingAnalyzeResponse.builder()
                .jobPostingId(jobPosting.getId())
                .jobPositionId(jobPosition.getId())
                .postingUrl(jobPosting.getPostingUrl())
                .title(jobPosting.getTitle())
                .companyName(analysis.getCompanyName())
                .positionName(analysis.getPositionName())
                .responsibilities(analysis.getResponsibilities())
                .requiredQualifications(analysis.getRequiredQualifications())
                .preferredQualifications(analysis.getPreferredQualifications())
                .techStack(analysis.getTechStack())
                .experienceRequirements(analysis.getExperienceRequirements())
                .keywords(analysis.getKeywords())
                .summary(analysis.getSummary())
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
}
