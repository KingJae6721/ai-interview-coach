package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.Interview;
import com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse;
import com.aiinterview.dashboard.dto.DashboardStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardScoreTrendResponse;
import com.aiinterview.dashboard.dto.DashboardAnalyticsProjection;
import com.aiinterview.dashboard.dto.DashboardCategoryStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardDifficultyStatisticsProjection;
import com.aiinterview.admin.dto.AdminDashboardInterviewStatistics;
import com.aiinterview.admin.dto.AdminInterviewListResponse;
import com.aiinterview.admin.dto.AdminUserInterviewStatistics;
import com.aiinterview.interview.entity.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    long countByUserIdAndJobPostingId(Long userId, Long jobPostingId);

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
    DashboardStatisticsProjection findDashboardStatisticsByUserId(@Param("userId") Long userId);

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
    List<DashboardRecentInterviewResponse> findRecentDashboardInterviewsByUserId(
            @Param("userId") Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardScoreTrendResponse("
            + "interview.id, interview.title, interview.completedAt, feedback.overallScore) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "order by interview.completedAt desc")
    List<DashboardScoreTrendResponse> findRecentCompletedScoreTrendByUserId(
            @Param("userId") Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardAnalyticsProjection("
            + "cast(function('date_trunc', 'week', interview.completedAt) as LocalDateTime), "
            + "avg(feedback.overallScore), count(interview)) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "group by cast(function('date_trunc', 'week', interview.completedAt) as LocalDateTime) "
            + "order by cast(function('date_trunc', 'week', interview.completedAt) as LocalDateTime) desc")
    List<DashboardAnalyticsProjection> findWeeklyDashboardAnalyticsByUserId(
            @Param("userId") Long userId, Pageable pageable);

    @Query("select new com.aiinterview.dashboard.dto.DashboardAnalyticsProjection("
            + "truncate(interview.completedAt, month), avg(feedback.overallScore), count(interview)) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "group by truncate(interview.completedAt, month) "
            + "order by truncate(interview.completedAt, month) desc")
    List<DashboardAnalyticsProjection> findMonthlyDashboardAnalyticsByUserId(
            @Param("userId") Long userId, Pageable pageable);

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
    List<DashboardCategoryStatisticsProjection> findCategoryStatisticsByUserId(@Param("userId") Long userId);

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
    List<DashboardDifficultyStatisticsProjection> findDifficultyStatisticsByUserId(@Param("userId") Long userId);

    @Query("select interview from Interview interview join fetch interview.user where interview.id = :interviewId")
    Optional<Interview> findWithUserById(@Param("interviewId") Long interviewId);

    @Query("""
            select interview
            from Interview interview
            join fetch interview.user
            left join fetch interview.jobPosition jobPosition
            left join fetch jobPosition.company
            where interview.id = :interviewId
            """)
    Optional<Interview> findWithUserAndJobPositionAndCompanyById(@Param("interviewId") Long interviewId);

    @Query(value = """
            select new com.aiinterview.admin.dto.AdminInterviewListResponse(
                interview.id, interview.title, user.id, user.email, interview.status,
                analysis.summary, count(distinct question), count(distinct answer),
                interview.createdAt, interview.completedAt)
            from Interview interview
            join interview.user user
            left join JobPostingAnalysis analysis on analysis.jobPosting = interview.jobPosting
            left join InterviewQuestion question on question.interview = interview
            left join InterviewAnswer answer on answer.interviewQuestion = question
            where (:status is null or interview.status = :status)
              and (:userEmail = '' or lower(user.email) like lower(concat('%', :userEmail, '%')))
            group by interview.id, interview.title, user.id, user.email, interview.status,
                     analysis.summary, interview.createdAt, interview.completedAt
            """,
            countQuery = """
            select count(interview)
            from Interview interview
            join interview.user user
            where (:status is null or interview.status = :status)
              and (:userEmail = '' or lower(user.email) like lower(concat('%', :userEmail, '%')))
            """)
    Page<AdminInterviewListResponse> findAdminInterviews(
            @Param("status") InterviewStatus status,
            @Param("userEmail") String userEmail,
            Pageable pageable);

    @Query("""
            select interview
            from Interview interview
            join fetch interview.user
            left join fetch interview.jobPosting jobPosting
            left join fetch jobPosting.jobPosition jobPosition
            left join fetch jobPosition.company
            left join fetch interview.resume
            where interview.id = :interviewId
            """)
    Optional<Interview> findAdminDetailById(@Param("interviewId") Long interviewId);

    @Query("""
            select new com.aiinterview.admin.dto.AdminUserInterviewStatistics(
                count(interview),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.READY then 1 else 0 end), 0),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.IN_PROGRESS then 1 else 0 end), 0),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED then 1 else 0 end), 0),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.CANCELLED then 1 else 0 end), 0))
            from Interview interview
            where interview.user.id = :userId
            """)
    AdminUserInterviewStatistics findAdminUserInterviewStatistics(@Param("userId") Long userId);

    Optional<Interview> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            select new com.aiinterview.admin.dto.AdminDashboardInterviewStatistics(
                count(interview),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.READY then 1 else 0 end), 0),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.IN_PROGRESS then 1 else 0 end), 0),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED then 1 else 0 end), 0),
                coalesce(sum(case when interview.status = com.aiinterview.interview.entity.InterviewStatus.CANCELLED then 1 else 0 end), 0),
                coalesce(sum(case when interview.createdAt >= :todayStart then 1 else 0 end), 0))
            from Interview interview
            """)
    AdminDashboardInterviewStatistics findAdminDashboardStatistics(
            @Param("todayStart") java.time.LocalDateTime todayStart);
}
