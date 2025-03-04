package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private String status; // 모집 상태

    @Column(length = 500)
    private String description; // 프로젝트 설명

    @Transient
    private String dDay;

    @ElementCollection
    private List<Long> likedUsers = new ArrayList<>(); // 좋아요 누른 유저들

    @Column(nullable = false)
    private int currentParticipants; // 실제 참여 인원

    public void setDDay(String dDay) {
        this.dDay = dDay;
    }

    // ✅ @PrePersist 추가 (데이터 저장 전에 기본값 설정)
    @PrePersist
    public void prePersist() {
        if (this.recruitmentStartDate == null) {
            this.recruitmentStartDate = LocalDate.now();  // 기본값: 오늘 날짜
        }
        if (this.recruitmentEndDate == null) {
            this.recruitmentEndDate = LocalDate.now().plusDays(7);  // 기본값: 7일 후
        }
    }
}
