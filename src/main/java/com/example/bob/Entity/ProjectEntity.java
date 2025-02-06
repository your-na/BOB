package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "project")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 프로젝트 ID

    @Column(length = 100, nullable = false)
    private String title; // 프로젝트명

    @Column(length = 100)
    private String createdBy; // 생성자

    @Column(nullable = false)
    private int recruitmentPeriod; // 모집 기간

    @Column(nullable = false)
    private LocalDate startDate; // 시작 날짜

    @Column(nullable = false)
    private LocalDate endDate; // 종료 날짜

    @Column(nullable = false)
    private int recruitmentCount; // 모집 인원

    @Column(nullable = false)
    private int views; // 조회수

    @Column(nullable = false)
    private int likes; // 좋아요

    @Column(length = 50, nullable = false)
    private String status; // 모집 상태 (예: 모집중)

    @Column(length = 500)
    private String description; // 설명

    @Column(length = 255, nullable = false)
    private String creatorNick; // 생성자 닉네임

    @PrePersist
    public void prePersist() {
        if (this.creatorNick == null || this.creatorNick.isEmpty()) {
            this.creatorNick = "default_nick"; // 기본값 설정
        }
    }
}
