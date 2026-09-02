package com.aiinterview.company.entity;

import com.aiinterview.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies", uniqueConstraints =
        @UniqueConstraint(name = "uk_companies_normalized_name", columnNames = "normalized_name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", length = 100)
    private String normalizedName;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Builder
    private Company(String name, String normalizedName, String websiteUrl, String logoUrl) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.websiteUrl = websiteUrl;
        this.logoUrl = logoUrl;
    }
}
