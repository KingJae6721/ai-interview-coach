package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @EntityGraph(attributePaths = {"jobPosition", "jobPosition.company"})
    Page<Interview> findByUserId(Long userId, Pageable pageable);

    @Query("select interview from Interview interview join fetch interview.user where interview.id = :interviewId")
    Optional<Interview> findWithUserById(Long interviewId);
}
