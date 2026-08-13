package com.aiinterview.dashboard.dto;

import lombok.Builder;
import lombok.Getter;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;

import java.util.List;

@Getter
public class DashboardWeaknessResponse {

    private final boolean performanceAnalysisAvailable;
    private final String unavailableReason;
    private final InterviewQuestionCategory weakestCategory;
    private final InterviewQuestionDifficulty weakestDifficulty;
    private final List<DashboardCategoryStatisticsResponse> categoryStatistics;
    private final List<DashboardDifficultyStatisticsResponse> difficultyStatistics;

    @Builder
    public DashboardWeaknessResponse(boolean performanceAnalysisAvailable, String unavailableReason,
                                     InterviewQuestionCategory weakestCategory,
                                     InterviewQuestionDifficulty weakestDifficulty,
                                     List<DashboardCategoryStatisticsResponse> categoryStatistics,
                                     List<DashboardDifficultyStatisticsResponse> difficultyStatistics) {
        this.performanceAnalysisAvailable = performanceAnalysisAvailable;
        this.unavailableReason = unavailableReason;
        this.weakestCategory = weakestCategory;
        this.weakestDifficulty = weakestDifficulty;
        this.categoryStatistics = categoryStatistics;
        this.difficultyStatistics = difficultyStatistics;
    }
}
