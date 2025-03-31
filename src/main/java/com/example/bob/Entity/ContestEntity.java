package com.example.bob.Entity;

import com.example.bob.DTO.ContestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;               // 공모전명
    private String organizer;           // 주최&주관
    private String category;            // 대표분야
    private String target;              // 참가대상
    private String region;              // 대회지역

    private LocalDate startDate;        // 진행 시작일
    private LocalDate endDate;          // 진행 종료일

    @Lob
    private String awardDetails;        // 시상내역 (길어질 수 있음)

    private String applicationMethod;   // 응모방법

    private String judge;               // 심사 기관

    @Lob
    private String description;         // 상세내용

    private String imageUrl;            // 공모전 포스터 이미지 URL

    private String status;              // 상태값 (예: 모집중, 종료 등)

    // 작성자
    private String creatorType; // "ADMIN" 또는 "COMPANY"

    // 승인 여부
    private boolean isApproved = false;

    // only BOB표시
    private boolean isOnlyBOB = false;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ContestDTO toDTO() {
        return ContestDTO.builder()
                .id(this.id)
                .title(this.title)
                .organizer(this.organizer)
                .category(this.category)
                .target(this.target)
                .region(this.region)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .judge(this.judge)
                .awardDetails(this.awardDetails)
                .applicationMethod(this.applicationMethod)
                .description(this.description)
                .status(this.status)
                .creatorType(this.creatorType)
                .isApproved(this.isApproved)
                .isOnlyBOB(this.isOnlyBOB)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
