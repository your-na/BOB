package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "project_history")
public class ProjectHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 히스토리 ID

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;  // ✅ 원본 프로젝트와 연관 관계 설정

    @Column(length = 100, nullable = false)
    private String title;  // 프로젝트명

    @Column(length = 500)
    private String description;  // 내용

    @Column(length = 500)
    private String goal;  // 프로젝트 목표

    @Column(nullable = false)
    private String createdBy;  // 작성자

    @Column(nullable = false)
    private LocalDate startDate;  // ✅ 진행 시작일

    @Column(nullable = false)
    private LocalDate endDate;  // ✅ 진행 종료일

    @Column(nullable = false)
    private int recruitmentPeriod;  // ✅ 모집 기간

    @Column(nullable = false)
    private LocalDateTime modifiedAt;  // ✅ 수정/삭제된 날짜

    @Column(length = 50, nullable = false)
    private String actionType;  // "수정됨" 또는 "삭제됨"
}
