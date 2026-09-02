package com.aiinterview.jobposition.repository;

import com.aiinterview.jobposition.entity.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {

    @Query("select jobPosition from JobPosition jobPosition join fetch jobPosition.company order by jobPosition.id")
    List<JobPosition> findAllWithCompany();

    @Query("select jobPosition from JobPosition jobPosition join fetch jobPosition.company where jobPosition.id = :jobPositionId")
    Optional<JobPosition> findWithCompanyById(@Param("jobPositionId") Long jobPositionId);

    Optional<JobPosition> findFirstByCompanyIdAndNormalizedName(Long companyId, String normalizedName);

}
