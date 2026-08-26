package com.aiinterview.resume.service;

import com.aiinterview.ai.dto.ResumeAnalysisResult;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.resume.dto.ResumeAnalyzeResponse;
import com.aiinterview.resume.entity.Resume;
import com.aiinterview.resume.entity.ResumeAnalysis;
import com.aiinterview.resume.extract.ExtractedResumeContent;
import com.aiinterview.resume.repository.ResumeAnalysisRepository;
import com.aiinterview.resume.repository.ResumeRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
class ResumePersistenceService {
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Transactional
    public ResumeAnalyzeResponse save(Long userId, MultipartFile file, ExtractedResumeContent extracted,
                                      ResumeAnalysisResult result) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Resume resume = resumeRepository.save(Resume.builder()
                .user(user)
                .originalFileName(sanitizeFileName(file.getOriginalFilename()))
                .fileSize(file.getSize())
                .contentType("application/pdf")
                .fileHash(extracted.sha256())
                .extractedText(extracted.text())
                .build());
        ResumeAnalysis analysis = resumeAnalysisRepository.save(ResumeAnalysis.builder()
                .resume(resume).summary(result.getSummary()).skills(result.getSkills())
                .workExperiences(result.getWorkExperiences()).projects(result.getProjects())
                .education(result.getEducation()).certifications(result.getCertifications())
                .achievements(result.getAchievements()).strengths(result.getStrengths())
                .keywords(result.getKeywords()).aiModel(result.getAiModel())
                .analyzedAt(LocalDateTime.now()).build());
        return toResponse(resume, analysis);
    }

    private String sanitizeFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "resume.pdf";
        }
        String normalized = originalFileName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "_").trim();
        if (!StringUtils.hasText(fileName)) {
            return "resume.pdf";
        }
        return fileName.length() > 255 ? fileName.substring(fileName.length() - 255) : fileName;
    }

    private ResumeAnalyzeResponse toResponse(Resume resume, ResumeAnalysis analysis) {
        return ResumeAnalyzeResponse.builder().resumeId(resume.getId()).originalFileName(resume.getOriginalFileName())
                .fileSize(resume.getFileSize()).summary(analysis.getSummary()).skills(analysis.getSkills())
                .workExperiences(analysis.getWorkExperiences()).projects(analysis.getProjects())
                .education(analysis.getEducation()).certifications(analysis.getCertifications())
                .achievements(analysis.getAchievements()).strengths(analysis.getStrengths())
                .keywords(analysis.getKeywords()).analyzedAt(analysis.getAnalyzedAt()).build();
    }
}
