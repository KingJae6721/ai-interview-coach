package com.aiinterview.company.repository;

import com.aiinterview.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findFirstByNormalizedName(String normalizedName);
}
