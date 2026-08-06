package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("select interview from Interview interview join fetch interview.user where interview.id = :interviewId")
    Optional<Interview> findWithUserById(Long interviewId);
}
