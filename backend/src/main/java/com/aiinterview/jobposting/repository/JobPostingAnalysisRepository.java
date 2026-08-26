package com.aiinterview.jobposting.repository;

import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JobPostingAnalysisRepository extends JpaRepository<JobPostingAnalysis, Long> {

    @Query("""
            select analysis
            from JobPostingAnalysis analysis
            join fetch analysis.jobPosting jobPosting
            join fetch jobPosting.jobPosition jobPosition
            join fetch jobPosition.company
            where jobPosting.id = :jobPostingId
            """)
    Optional<JobPostingAnalysis> findWithJobPostingAndJobPositionAndCompanyByJobPostingId(Long jobPostingId);
}
