package com.aiinterview.dashboard.service;

import com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse;
import com.aiinterview.dashboard.dto.DashboardStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardSummaryResponse;
import com.aiinterview.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_INTERVIEW_LIMIT = 5;

    private final InterviewRepository interviewRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long userId) {
        DashboardStatisticsProjection statistics = interviewRepository.findDashboardStatisticsByUserId(userId);
        List<DashboardRecentInterviewResponse> recentInterviews = interviewRepository
                .findRecentDashboardInterviewsByUserId(userId, PageRequest.of(0, RECENT_INTERVIEW_LIMIT));

        return DashboardSummaryResponse.builder()
                .totalInterviews(statistics.getTotalInterviews())
                .completedInterviews(statistics.getCompletedInterviews())
                .averageScore(statistics.getAverageScore())
                .highestScore(statistics.getHighestScore())
                .latestInterviewAt(statistics.getLatestInterviewAt())
                .recentInterviews(recentInterviews)
                .build();
    }
}
