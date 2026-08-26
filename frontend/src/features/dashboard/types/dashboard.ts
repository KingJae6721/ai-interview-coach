import type {
  InterviewStatus,
  QuestionCategory,
  QuestionDifficulty,
} from "@/features/interview/types/interview";

export interface DashboardRecentInterviewResponse {
  interviewId: number;
  title: string;
  status: InterviewStatus;
  createdAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
  companyName: string | null;
  positionName: string | null;
  overallScore: number | null;
  feedbackExists: boolean;
  partial: boolean;
}

export interface DashboardSummaryResponse {
  totalInterviews: number;
  completedInterviews: number;
  cancelledInterviews: number;
  averageScore: number | null;
  highestScore: number | null;
  latestInterviewAt: string | null;
  recentInterviews: DashboardRecentInterviewResponse[];
}

export interface DashboardScoreTrendResponse {
  interviewId: number;
  title: string;
  completedAt: string;
  overallScore: number;
}

export type DashboardAnalyticsPeriod = "WEEKLY" | "MONTHLY";

export interface DashboardAnalyticsResponse {
  periodStartAt: string;
  averageScore: number;
  interviewCount: number;
  scoreChange: number | null;
}

export interface DashboardCategoryStatisticsResponse {
  category: QuestionCategory;
  interviewCount: number;
  questionCount: number;
  evaluationCount: number;
  averageScore: number | null;
}

export interface DashboardDifficultyStatisticsResponse {
  difficulty: QuestionDifficulty;
  interviewCount: number;
  questionCount: number;
  evaluationCount: number;
  averageScore: number | null;
}

export interface DashboardWeaknessResponse {
  performanceAnalysisAvailable: boolean;
  unavailableReason: string | null;
  weakestCategory: QuestionCategory | null;
  weakestDifficulty: QuestionDifficulty | null;
  categoryStatistics: DashboardCategoryStatisticsResponse[];
  difficultyStatistics: DashboardDifficultyStatisticsResponse[];
}
