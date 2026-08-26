package com.aiinterview.jobposting.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.jobposition.entity.JobPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_position_id", nullable = false)
    private JobPosition jobPosition;

    @Column(name = "posting_url", nullable = false, columnDefinition = "TEXT")
    private String postingUrl;

    @Column(length = 255)
    private String title;

    @Column(name = "extracted_content", nullable = false, columnDefinition = "TEXT")
    private String extractedContent;

    @Builder
    private JobPosting(JobPosition jobPosition, String postingUrl, String title, String extractedContent) {
        this.jobPosition = jobPosition;
        this.postingUrl = postingUrl;
        this.title = title;
        this.extractedContent = extractedContent;
    }
}
