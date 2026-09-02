package com.aiinterview.jobposting.service;

import com.aiinterview.jobposting.dto.JobPostingAnalyzeRequest;
import com.aiinterview.jobposting.dto.JobPostingAnalyzeResponse;
import com.aiinterview.jobposting.dto.JobPostingSummaryResponse;

import java.util.List;

public interface JobPostingService {

    JobPostingAnalyzeResponse analyzeJobPosting(Long userId, JobPostingAnalyzeRequest request);

    List<JobPostingSummaryResponse> getJobPostings(Long userId);
}
