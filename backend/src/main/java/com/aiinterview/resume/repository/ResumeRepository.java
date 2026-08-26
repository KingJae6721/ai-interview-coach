package com.aiinterview.resume.repository;

import com.aiinterview.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
