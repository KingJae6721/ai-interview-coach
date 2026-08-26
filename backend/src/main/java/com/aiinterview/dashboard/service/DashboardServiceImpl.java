package com.aiinterview.dashboard.service;

import com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse;
import com.aiinterview.dashboard.dto.DashboardStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardSummaryResponse;
import com.aiinterview.dashboard.dto.DashboardScoreTrendResponse;
import com.aiinterview.dashboard.dto.DashboardAnalyticsPeriod;
import com.aiinterview.dashboard.dto.DashboardAnalyticsProjection;
import com.aiinterview.dashboard.dto.DashboardAnalyticsResponse;
import com.aiinterview.dashboard.dto.DashboardCategoryStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardCategoryStatisticsResponse;
import com.aiinterview.dashboard.dto.DashboardDifficultyStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardDifficultyStatisticsResponse;
import com.aiinterview.dashboard.dto.DashboardWeaknessResponse;
import com.aiinterview.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_INTERVIEW_LIMIT = 5;
    private static final String PERFORMANCE_ANALYSIS_UNAVAILABLE_REASON =
            "Question evaluations are required for performance analysis.";

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
                .cancelledInterviews(statistics.getCancelledInterviews())
                .averageScore(statistics.getAverageScore())
                .highestScore(statistics.getHighestScore())
                .latestInterviewAt(statistics.getLatestInterviewAt())
                .recentInterviews(recentInterviews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardScoreTrendResponse> getScoreTrend(Long userId, int limit) {
        List<DashboardScoreTrendResponse> scoreTrend = new ArrayList<>(interviewRepository
                .findRecentCompletedScoreTrendByUserId(userId, PageRequest.of(0, limit)));

        Collections.reverse(scoreTrend);
        return scoreTrend;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardAnalyticsResponse> getAnalytics(Long userId, DashboardAnalyticsPeriod period, int limit) {
        List<DashboardAnalyticsProjection> analytics = new ArrayList<>(interviewRepository
                .findDashboardAnalyticsByUserId(userId, period.getDateTruncUnit(), PageRequest.of(0, limit)));

        Collections.reverse(analytics);

        List<DashboardAnalyticsResponse> responses = new ArrayList<>();
        Double previousAverageScore = null;
        for (DashboardAnalyticsProjection analyticsPeriod : analytics) {
            Double averageScore = analyticsPeriod.getAverageScore();
            responses.add(DashboardAnalyticsResponse.builder()
                    .periodStartAt(analyticsPeriod.getPeriodStartAt())
                    .averageScore(averageScore)
                    .interviewCount(analyticsPeriod.getInterviewCount())
                    .scoreChange(previousAverageScore == null ? null : averageScore - previousAverageScore)
                    .build());
            previousAverageScore = averageScore;
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardWeaknessResponse getWeaknesses(Long userId) {
        List<DashboardCategoryStatisticsResponse> categoryStatistics = interviewRepository
                .findCategoryStatisticsByUserId(userId).stream()
                .map(this::toCategoryStatisticsResponse)
                .toList();
        List<DashboardDifficultyStatisticsResponse> difficultyStatistics = interviewRepository
                .findDifficultyStatisticsByUserId(userId).stream()
                .map(this::toDifficultyStatisticsResponse)
                .toList();

        boolean performanceAnalysisAvailable = categoryStatistics.stream()
                .anyMatch(statistics -> statistics.getEvaluationCount() > 0)
                || difficultyStatistics.stream().anyMatch(statistics -> statistics.getEvaluationCount() > 0);

        return DashboardWeaknessResponse.builder()
                .performanceAnalysisAvailable(performanceAnalysisAvailable)
                .unavailableReason(performanceAnalysisAvailable ? null : PERFORMANCE_ANALYSIS_UNAVAILABLE_REASON)
                .weakestCategory(categoryStatistics.stream()
                        .filter(statistics -> statistics.getEvaluationCount() > 0)
                        .map(DashboardCategoryStatisticsResponse::getCategory)
                        .findFirst()
                        .orElse(null))
                .weakestDifficulty(difficultyStatistics.stream()
                        .filter(statistics -> statistics.getEvaluationCount() > 0)
                        .map(DashboardDifficultyStatisticsResponse::getDifficulty)
                        .findFirst()
                        .orElse(null))
                .categoryStatistics(categoryStatistics)
                .difficultyStatistics(difficultyStatistics)
                .build();
    }

    private DashboardCategoryStatisticsResponse toCategoryStatisticsResponse(
            DashboardCategoryStatisticsProjection projection) {
        return DashboardCategoryStatisticsResponse.builder()
                .category(projection.getCategory())
                .interviewCount(projection.getInterviewCount())
                .questionCount(projection.getQuestionCount())
                .evaluationCount(projection.getEvaluationCount())
                .averageScore(projection.getAverageScore())
                .build();
    }

    private DashboardDifficultyStatisticsResponse toDifficultyStatisticsResponse(
            DashboardDifficultyStatisticsProjection projection) {
        return DashboardDifficultyStatisticsResponse.builder()
                .difficulty(projection.getDifficulty())
                .interviewCount(projection.getInterviewCount())
                .questionCount(projection.getQuestionCount())
                .evaluationCount(projection.getEvaluationCount())
                .averageScore(projection.getAverageScore())
                .build();
    }
}
