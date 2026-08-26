package com.aiinterview.resume.extract;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeTextExtractor {
    ExtractedResumeContent extract(MultipartFile file);
}
