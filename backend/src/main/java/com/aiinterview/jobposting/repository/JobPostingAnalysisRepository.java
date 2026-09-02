package com.aiinterview.jobposting.repository;

import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface JobPostingAnalysisRepository extends JpaRepository<JobPostingAnalysis, Long> {

    @Query("""
            select analysis
            from JobPostingAnalysis analysis
            join fetch analysis.jobPosting jobPosting
            join fetch jobPosting.user
            join fetch jobPosting.jobPosition jobPosition
            join fetch jobPosition.company
            where jobPosting.id = :jobPostingId
            """)
    Optional<JobPostingAnalysis> findWithJobPostingAndJobPositionAndCompanyByJobPostingId(
            @Param("jobPostingId") Long jobPostingId);

    @Query("""
            select analysis
            from JobPostingAnalysis analysis
            join fetch analysis.jobPosting jobPosting
            join fetch jobPosting.user
            where jobPosting.user.id = :userId
            order by analysis.analyzedAt desc
            """)
    List<JobPostingAnalysis> findAllWithJobPostingByUserId(@Param("userId") Long userId);
}
