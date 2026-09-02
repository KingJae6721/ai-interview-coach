package com.aiinterview.jobposition.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.common.util.NormalizedNameNormalizer;
import com.aiinterview.company.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "job_positions", uniqueConstraints =
        @UniqueConstraint(name = "uk_job_positions_company_normalized_name",
                columnNames = {"company_id", "normalized_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tech_stack", columnDefinition = "jsonb")
    private List<String> techStack;

    @Column(name = "interview_criteria", columnDefinition = "TEXT")
    private String interviewCriteria;

    @Builder
    private JobPosition(Company company, String name, List<String> techStack, String interviewCriteria) {
        this.company = company;
        this.name = name;
        this.normalizedName = NormalizedNameNormalizer.normalize(name)
                .map(NormalizedNameNormalizer.NormalizedName::value)
                .orElse(null);
        this.techStack = techStack == null ? null : List.copyOf(techStack);
        this.interviewCriteria = interviewCriteria;
    }
}
