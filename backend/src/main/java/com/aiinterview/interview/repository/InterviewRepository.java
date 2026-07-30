package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
}
