package com.aiinterview.jobposting.service;

import com.aiinterview.jobposting.dto.JobPostingAnalyzeRequest;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeResponse;

public interface JobPostingService {

    JobPostingAnalyzeResponse analyzeJobPosting(JobPostingAnalyzeRequest request);
}
