package com.aiinterview.resume.service;

import com.aiinterview.ai.dto.ResumeAnalysisResult;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.resume.dto.ResumeAnalyzeResponse;
import com.aiinterview.resume.dto.ResumeSummaryResponse;
import com.aiinterview.resume.entity.ResumeAnalysis;
import com.aiinterview.resume.extract.ExtractedResumeContent;
import com.aiinterview.resume.extract.ResumeTextExtractor;
import com.aiinterview.resume.repository.ResumeAnalysisRepository;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {
    private final UserRepository userRepository;
    private final ResumeTextExtractor resumeTextExtractor;
    private final AiService aiService;
    private final ResumePersistenceService resumePersistenceService;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResumeAnalyzeResponse analyzeResume(Long userId, MultipartFile file) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        ExtractedResumeContent extracted = resumeTextExtractor.extract(file);
        ResumeAnalysisResult analysis = aiService.analyzeResume(extracted.text());
        return resumePersistenceService.save(userId, file, extracted, analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeSummaryResponse> getResumes(Long userId) {
        return resumeAnalysisRepository.findAllWithResumeByUserId(userId).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private ResumeSummaryResponse toSummaryResponse(ResumeAnalysis analysis) {
        return ResumeSummaryResponse.builder()
                .resumeId(analysis.getResume().getId())
                .originalFileName(analysis.getResume().getOriginalFileName())
                .createdAt(analysis.getResume().getCreatedAt())
                .summary(analysis.getSummary())
                .skills(analysis.getSkills())
                .build();
    }
}
