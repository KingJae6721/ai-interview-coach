package com.aiinterview.resume.service;

import com.aiinterview.resume.dto.ResumeAnalyzeResponse;
import com.aiinterview.resume.dto.ResumeSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {
    ResumeAnalyzeResponse analyzeResume(Long userId, MultipartFile file);
    List<ResumeSummaryResponse> getResumes(Long userId);
}
