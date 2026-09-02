package com.aiinterview.resume.repository;

import com.aiinterview.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    @Query("select resume from Resume resume join fetch resume.user where resume.id = :resumeId")
    Optional<Resume> findWithUserById(@Param("resumeId") Long resumeId);
}
