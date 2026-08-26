package com.aiinterview.jobposting.entity;

import com.aiinterview.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_posting_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPostingAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false, unique = true)
    private JobPosting jobPosting;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "position_name", length = 100)
    private String positionName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> responsibilities;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_qualifications", columnDefinition = "jsonb", nullable = false)
    private List<String> requiredQualifications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_qualifications", columnDefinition = "jsonb", nullable = false)
    private List<String> preferredQualifications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tech_stack", columnDefinition = "jsonb", nullable = false)
    private List<String> techStack;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience_requirements", columnDefinition = "jsonb", nullable = false)
    private List<String> experienceRequirements;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> keywords;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ai_model", nullable = false, length = 100)
    private String aiModel;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Builder
    private JobPostingAnalysis(JobPosting jobPosting, String companyName, String positionName,
                               List<String> responsibilities, List<String> requiredQualifications,
                               List<String> preferredQualifications, List<String> techStack,
                               List<String> experienceRequirements, List<String> keywords,
                               String summary, String aiModel, LocalDateTime analyzedAt) {
        this.jobPosting = jobPosting;
        this.companyName = companyName;
        this.positionName = positionName;
        this.responsibilities = List.copyOf(responsibilities);
        this.requiredQualifications = List.copyOf(requiredQualifications);
        this.preferredQualifications = List.copyOf(preferredQualifications);
        this.techStack = List.copyOf(techStack);
        this.experienceRequirements = List.copyOf(experienceRequirements);
        this.keywords = List.copyOf(keywords);
        this.summary = summary;
        this.aiModel = aiModel;
        this.analyzedAt = analyzedAt;
    }
}
