package com.aiinterview.resume.repository;

import com.aiinterview.resume.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    @Query("""
            select analysis from ResumeAnalysis analysis
            join fetch analysis.resume resume
            where resume.user.id = :userId
            order by resume.createdAt desc
            """)
    List<ResumeAnalysis> findAllWithResumeByUserId(Long userId);
}
