package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // ✅ 기존 엔티티 수정 가능하게 설정
@Table(name = "project")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 프로젝트 ID

    @Column(length = 100, nullable = false)
    private String title; // 프로젝트명

    @Column(length = 500, nullable = true)
    private String goal;  // 프로젝트 목표

    @Column(length = 255, nullable = false)
    private String createdBy; // 작성자

    @Column(length = 255, nullable = false)
    private String creatorNick; // 생성자 닉네임

    @Column(nullable = false)
    private int recruitmentPeriod; // 모집 기간

    @Column(nullable = false)
    private LocalDate startDate; // 시작 날짜

    @Column(nullable = false)
    private LocalDate endDate; // 종료 날짜

    @Column(name = "recruitment_start_date", nullable = false)
    private LocalDate recruitmentStartDate;

    @Column(name = "recruitment_end_date", nullable = false)
    private LocalDate recruitmentEndDate;

    @Column(nullable = false)
    private int recruitmentCount; // 모집 인원

    @Column(nullable = false)
    private int views; // 조회수

    @Column(nullable = false)
    private int likes; // 좋아요 개수

    @Column(length = 50, nullable = false)
    @Builder.Default
    private String status = "모집중"; // ✅ 기본값을 한글로 설정 ("모집중", "진행중")

    @Column(length = 500)
    private String description; // 프로젝트 설명

    @Column(nullable = true)
    private Long dDay;  // long -> Long로 변경

    @ElementCollection
    private List<Long> likedUsers = new ArrayList<>(); // 좋아요 누른 유저들

    @Column(nullable = false)
    private int currentParticipants; // 실제 참여 인원

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectHistoryEntity> projectHistoryEntities = new ArrayList<>();

    // ✅ 상태를 한글로 자동 설정 (모집중 / 진행중)
    public void updateStatus() {
        LocalDate today = LocalDate.now();

        // 모집 종료일 기준으로 상태 업데이트
        if (today.isAfter(this.recruitmentEndDate)) {
            this.status = "진행중"; // 모집 종료일이 지나면 진행중
        } else {
            this.status = "모집중"; // 모집 종료일 전에는 모집중
        }
    }

    @PrePersist
    @PreUpdate
    public void calculateDDay() {
        LocalDate today = LocalDate.now();  // 오늘 날짜
        LocalDate endDate = this.recruitmentEndDate;  // 모집 종료일

        if (endDate == null) {
            this.dDay = 0L;  // 종료일이 없으면 디데이는 0으로 설정
            return;
        }

        long daysBetween = today.until(endDate, ChronoUnit.DAYS);  // 오늘부터 종료일까지의 일수 계산

        if (daysBetween < 0) {
            this.dDay = 0L;  // 종료일이 이미 지나면 D-Day는 0
        } else {
            this.dDay = daysBetween;  // 남은 일수
        }
    }
    }



