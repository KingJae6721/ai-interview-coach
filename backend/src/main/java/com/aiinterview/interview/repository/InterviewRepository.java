package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.Interview;
import com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse;
import com.aiinterview.dashboard.dto.DashboardStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardScoreTrendResponse;
import com.aiinterview.dashboard.dto.DashboardAnalyticsProjection;
import com.aiinterview.dashboard.dto.DashboardCategoryStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardDifficultyStatisticsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @EntityGraph(attributePaths = {"jobPosition", "jobPosition.company"})
    Page<Interview> findByUserId(Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardStatisticsProjection("
            + "count(interview), "
            + "coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "then 1 else 0 end), 0), "
            + "coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.CANCELLED "
            + "then 1 else 0 end), 0), "
            + "avg(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "then feedback.overallScore else null end), "
            + "max(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "then feedback.overallScore else null end), max(interview.createdAt)) "
            + "from Interview interview "
            + "left join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId")
    DashboardStatisticsProjection findDashboardStatisticsByUserId(Long userId);

    @Query("select new com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse("
            + "interview.id, interview.title, interview.status, interview.createdAt, interview.completedAt, "
            + "interview.cancelledAt, company.name, jobPosition.name, feedback.overallScore, "
            + "case when feedback.id is not null then true else false end, coalesce(feedback.partial, false)) "
            + "from Interview interview "
            + "left join interview.jobPosition jobPosition "
            + "left join jobPosition.company company "
            + "left join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "order by interview.createdAt desc")
    List<DashboardRecentInterviewResponse> findRecentDashboardInterviewsByUserId(Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardScoreTrendResponse("
            + "interview.id, interview.title, interview.completedAt, feedback.overallScore) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "order by interview.completedAt desc")
    List<DashboardScoreTrendResponse> findRecentCompletedScoreTrendByUserId(Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardAnalyticsProjection("
            + "cast(function('date_trunc', 'week', interview.completedAt) as LocalDateTime), "
            + "avg(feedback.overallScore), count(interview)) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "group by cast(function('date_trunc', 'week', interview.completedAt) as LocalDateTime) "
            + "order by cast(function('date_trunc', 'week', interview.completedAt) as LocalDateTime) desc")
    List<DashboardAnalyticsProjection> findWeeklyDashboardAnalyticsByUserId(Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardAnalyticsProjection("
            + "truncate(interview.completedAt, month), avg(feedback.overallScore), count(interview)) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "group by truncate(interview.completedAt, month) "
            + "order by truncate(interview.completedAt, month) desc")
    List<DashboardAnalyticsProjection> findMonthlyDashboardAnalyticsByUserId(Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardCategoryStatisticsProjection("
            + "question.category, count(distinct interview), count(question), count(evaluation), avg(evaluation.score)) "
            + "from InterviewQuestion question "
            + "join question.interview interview "
            + "left join InterviewAnswer answer on answer.interviewQuestion = question "
            + "left join QuestionEvaluation evaluation on evaluation.answer = answer "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "and question.category is not null "
            + "group by question.category "
            + "order by case when count(evaluation) = 0 then 1 else 0 end asc, "
            + "avg(evaluation.score) asc, count(evaluation) desc, question.category asc")
    List<DashboardCategoryStatisticsProjection> findCategoryStatisticsByUserId(Long userId);

    @Query("select new com.aiinterview.dashboard.dto.DashboardDifficultyStatisticsProjection("
            + "question.difficulty, count(distinct interview), count(question), count(evaluation), avg(evaluation.score)) "
            + "from InterviewQuestion question "
            + "join question.interview interview "
            + "left join InterviewAnswer answer on answer.interviewQuestion = question "
            + "left join QuestionEvaluation evaluation on evaluation.answer = answer "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "and question.difficulty is not null "
            + "group by question.difficulty "
            + "order by case when count(evaluation) = 0 then 1 else 0 end asc, "
            + "avg(evaluation.score) asc, count(evaluation) desc, question.difficulty asc")
    List<DashboardDifficultyStatisticsProjection> findDifficultyStatisticsByUserId(Long userId);

    @Query("select interview from Interview interview join fetch interview.user where interview.id = :interviewId")
    Optional<Interview> findWithUserById(Long interviewId);

    @Query("""
            select interview
            from Interview interview
            join fetch interview.user
            left join fetch interview.jobPosition jobPosition
            left join fetch jobPosition.company
            where interview.id = :interviewId
            """)
    Optional<Interview> findWithUserAndJobPositionAndCompanyById(Long interviewId);
}
