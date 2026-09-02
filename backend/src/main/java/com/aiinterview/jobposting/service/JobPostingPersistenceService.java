package com.aiinterview.jobposting.service;

import com.aiinterview.ai.dto.JobPostingAnalysisResult;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.common.util.NormalizedNameNormalizer;
import com.aiinterview.common.util.NormalizedNameNormalizer.NormalizedName;
import com.aiinterview.company.entity.Company;
import com.aiinterview.company.repository.CompanyRepository;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeResponse;
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import com.aiinterview.jobposting.fetch.FetchedJobPostingContent;
import com.aiinterview.jobposting.repository.JobPostingAnalysisRepository;
import com.aiinterview.jobposting.repository.JobPostingRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
class JobPostingPersistenceService {

    private static final int MAX_REFERENCE_NAME_LENGTH = 100;

    private final CompanyRepository companyRepository;
    private final JobPositionRepository jobPositionRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingAnalysisRepository jobPostingAnalysisRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobPostingAnalyzeResponse save(Long userId, String postingUrl, FetchedJobPostingContent fetchedContent,
                                          JobPostingAnalysisResult analysisResult) {
        JobPosition jobPosition = resolveJobPosition(analysisResult);
        User user = userRepository.getReferenceById(userId);

        JobPosting jobPosting = jobPostingRepository.save(JobPosting.builder()
                .user(user)
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

    @Transactional(readOnly = true)
    public List<com.aiinterview.jobposting.dto.JobPostingSummaryResponse> getByUserId(Long userId) {
        return jobPostingAnalysisRepository.findAllWithJobPostingByUserId(userId).stream()
                .map(analysis -> com.aiinterview.jobposting.dto.JobPostingSummaryResponse.builder()
                        .jobPostingId(analysis.getJobPosting().getId())
                        .postingUrl(analysis.getJobPosting().getPostingUrl())
                        .companyName(analysis.getCompanyName())
                        .positionName(analysis.getPositionName())
                        .summary(analysis.getSummary())
                        .techStack(analysis.getTechStack())
                        .analyzedAt(analysis.getAnalyzedAt())
                        .build())
                .toList();
    }

    private JobPosition resolveJobPosition(JobPostingAnalysisResult analysisResult) {
        NormalizedName companyName = normalizeRequiredName(analysisResult.getCompanyName());
        NormalizedName positionName = normalizeRequiredName(analysisResult.getPositionName());
        Company company = resolveCompany(companyName);

        return jobPositionRepository
                .findFirstByCompanyIdAndNormalizedName(company.getId(), positionName.value())
                .orElseGet(() -> jobPositionRepository.save(JobPosition.builder()
                        .company(company)
                        .name(positionName.displayName())
                        .techStack(analysisResult.getTechStack())
                        .interviewCriteria(buildInterviewCriteria(analysisResult))
                        .build()));
    }

    private Company resolveCompany(NormalizedName companyName) {
        return companyRepository.findFirstByNormalizedName(companyName.value())
                .orElseGet(() -> companyRepository.save(Company.builder()
                        .name(companyName.displayName())
                        .build()));
    }

    private NormalizedName normalizeRequiredName(String value) {
        return NormalizedNameNormalizer.normalize(value)
                .filter(name -> name.displayName().length() <= MAX_REFERENCE_NAME_LENGTH
                        && name.value().length() <= MAX_REFERENCE_NAME_LENGTH)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_ANALYSIS_INSUFFICIENT));
    }

    private String buildInterviewCriteria(JobPostingAnalysisResult analysisResult) {
        StringJoiner criteria = new StringJoiner("\n");
        addCriteria(criteria, "Responsibilities", analysisResult.getResponsibilities());
        addCriteria(criteria, "Required qualifications", analysisResult.getRequiredQualifications());
        addCriteria(criteria, "Preferred qualifications", analysisResult.getPreferredQualifications());
        addCriteria(criteria, "Experience requirements", analysisResult.getExperienceRequirements());
        return criteria.length() == 0 ? null : criteria.toString();
    }

    private void addCriteria(StringJoiner criteria, String label, List<String> values) {
        if (values != null && !values.isEmpty()) {
            criteria.add(label + ": " + String.join(", ", values));
        }
    }
}
