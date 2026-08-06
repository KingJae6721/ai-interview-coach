package com.aiinterview.jobposition.repository;

import com.aiinterview.jobposition.entity.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {

    @Query("select jobPosition from JobPosition jobPosition join fetch jobPosition.company where jobPosition.id = :jobPositionId")
    Optional<JobPosition> findWithCompanyById(Long jobPositionId);
}
