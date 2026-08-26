package com.aiinterview.resume.repository;

import com.aiinterview.resume.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    @Query("""
            select analysis from ResumeAnalysis analysis
            join fetch analysis.resume resume
            join fetch resume.user
            where resume.id = :resumeId
            """)
    Optional<ResumeAnalysis> findWithResumeAndUserByResumeId(Long resumeId);

    boolean existsByResumeId(Long resumeId);

    @Query("""
            select analysis from ResumeAnalysis analysis
            join fetch analysis.resume resume
            where resume.user.id = :userId
            order by resume.createdAt desc
            """)
    List<ResumeAnalysis> findAllWithResumeByUserId(Long userId);
}
