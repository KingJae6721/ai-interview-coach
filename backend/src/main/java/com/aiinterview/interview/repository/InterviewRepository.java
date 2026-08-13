package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.Interview;
import com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse;
import com.aiinterview.dashboard.dto.DashboardStatisticsProjection;
import com.aiinterview.dashboard.dto.DashboardScoreTrendResponse;
import com.aiinterview.dashboard.dto.DashboardAnalyticsProjection;
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
            + "avg(feedback.overallScore), max(feedback.overallScore), max(interview.createdAt)) "
            + "from Interview interview "
            + "left join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId")
    DashboardStatisticsProjection findDashboardStatisticsByUserId(Long userId);

    @Query("select new com.aiinterview.dashboard.dto.DashboardRecentInterviewResponse("
            + "interview.id, interview.title, interview.status, interview.createdAt, interview.completedAt, "
            + "company.name, jobPosition.name, feedback.overallScore) "
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
            + "function('date_trunc', :dateTruncUnit, interview.completedAt), "
            + "avg(feedback.overallScore), count(interview)) "
            + "from Interview interview "
            + "join Feedback feedback on feedback.interview = interview "
            + "where interview.user.id = :userId "
            + "and interview.status = com.aiinterview.interview.entity.InterviewStatus.COMPLETED "
            + "group by function('date_trunc', :dateTruncUnit, interview.completedAt) "
            + "order by function('date_trunc', :dateTruncUnit, interview.completedAt) desc")
    List<DashboardAnalyticsProjection> findDashboardAnalyticsByUserId(
            Long userId, String dateTruncUnit, Pageable pageable);

    @Query("select interview from Interview interview join fetch interview.user where interview.id = :interviewId")
    Optional<Interview> findWithUserById(Long interviewId);
}
