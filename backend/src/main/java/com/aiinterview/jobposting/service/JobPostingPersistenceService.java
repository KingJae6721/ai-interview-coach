package com.aiinterview.jobposting.service;

import com.aiinterview.ai.dto.JobPostingAnalysisResult;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
class JobPostingPersistenceService {

    private static final int MAX_REFERENCE_NAME_LENGTH = 100;

    private final CompanyRepository companyRepository;
    private final JobPositionRepository jobPositionRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingAnalysisRepository jobPostingAnalysisRepository;

    @Transactional
    public JobPostingAnalyzeResponse save(Long deprecatedJobPositionId, String postingUrl,
                                          FetchedJobPostingContent fetchedContent,
                                          JobPostingAnalysisResult analysisResult) {
        JobPosition jobPosition = resolveJobPosition(deprecatedJobPositionId, analysisResult);

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

    private JobPosition resolveJobPosition(Long deprecatedJobPositionId, JobPostingAnalysisResult analysisResult) {
        if (deprecatedJobPositionId != null) {
            return jobPositionRepository.findById(deprecatedJobPositionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSITION_NOT_FOUND));
        }

        NormalizedName companyName = normalizeRequiredName(analysisResult.getCompanyName());
        NormalizedName positionName = normalizeRequiredName(analysisResult.getPositionName());
        Company company = resolveCompany(companyName);

        return jobPositionRepository
                .findFirstByCompanyIdAndNormalizedName(company.getId(), positionName.normalized())
                .orElseGet(() -> findLegacyJobPosition(company.getId(), positionName.normalized())
                        .orElseGet(() -> jobPositionRepository.save(JobPosition.builder()
                                .company(company)
                                .name(positionName.display())
                                .normalizedName(positionName.normalized())
                                .techStack(analysisResult.getTechStack())
                                .interviewCriteria(buildInterviewCriteria(analysisResult))
                                .build())));
    }

    private Company resolveCompany(NormalizedName companyName) {
        return companyRepository.findFirstByNormalizedName(companyName.normalized())
                .orElseGet(() -> companyRepository.findAll().stream()
                        .filter(company -> normalizeForComparison(company.getName()).equals(companyName.normalized()))
                        .min(Comparator.comparing(Company::getId))
                        .orElseGet(() -> companyRepository.save(Company.builder()
                                .name(companyName.display())
                                .normalizedName(companyName.normalized())
                                .build())));
    }

    private Optional<JobPosition> findLegacyJobPosition(Long companyId, String normalizedPositionName) {
        return jobPositionRepository.findAllByCompanyIdOrderByIdAsc(companyId).stream()
                .filter(position -> normalizeForComparison(position.getName()).equals(normalizedPositionName))
                .findFirst();
    }

    private NormalizedName normalizeRequiredName(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.JOB_POSTING_ANALYSIS_INSUFFICIENT);
        }
        String display = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ");
        String normalized = display.toLowerCase(Locale.ROOT);
        if (display.isBlank() || display.length() > MAX_REFERENCE_NAME_LENGTH
                || normalized.length() > MAX_REFERENCE_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.JOB_POSTING_ANALYSIS_INSUFFICIENT);
        }
        return new NormalizedName(display, normalized);
    }

    private String normalizeForComparison(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
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

    private record NormalizedName(String display, String normalized) {
    }
}
