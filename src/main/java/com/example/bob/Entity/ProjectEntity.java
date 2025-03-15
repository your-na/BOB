package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;


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
    private String createdBy; // 프로젝트 생성자 (유저 닉네임)

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
    private String status = "모집중"; // ✅ 기본값 ("모집중", "진행중", "완료")

    @Column(length = 500)
    private String description; // 프로젝트 설명

    @Column(name = "d_day")
    private Integer dDay;  // ✅ int → Integer 변경 (null 값 허용)

    @ElementCollection
    private List<Long> likedUsers = new ArrayList<>(); // 좋아요 누른 유저들

    @Column(nullable = false)
    private int currentParticipants; // 실제 참여 인원

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProjectEntity> userProjects = new ArrayList<>();  // ✅ 초기화 추가

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectHistoryEntity> projectHistoryEntities = new ArrayList<>();

    // ✅ 프로젝트 진행 시작 시, 주최자만 `"진행중"`으로 변경
    public void startProject() {
        if (LocalDate.now().isEqual(this.startDate)) { // 진행일 시작되면
            this.status = "진행중";

            for (UserProjectEntity userProject : userProjects) {
                if (userProject.getUser().getUserNick().equals(this.createdBy)) {
                    userProject.setStatus("진행중"); // 주최자만 "진행중"
                }
            }
        }
    }

    // ✅ 특정 팀원이 수락되면 그 팀원만 `"진행중"`으로 변경
    public void approveTeamMember(UserEntity user) {
        for (UserProjectEntity userProject : userProjects) {
            if (userProject.getUser().equals(user) && userProject.getStatus().equals("신청중")) {
                userProject.setStatus("진행중");
            }
        }
    }

    // ✅ 주최자가 제출을 하면 "진행중" 상태의 팀원들만 `"완료"`로 변경
    public void completeProject() {
        for (UserProjectEntity userProject : userProjects) {
            // 🔥 "진행중" 상태의 팀원만 "완료"로 변경
            if (userProject.getUser().getUserNick().equals(this.createdBy)
                    && userProject.getSubmittedFileName() != null) {
                this.status = "완료"; // 프로젝트 상태 변경

                for (UserProjectEntity member : userProjects) {
                    if (member.getStatus().equals("진행중")) {
                        member.setStatus("완료"); // 🔥 "진행중"인 팀원만 "완료"
                    }
                }
                break;
            }
        }
    }

    // ✅ 주최자의 상태에 따라 프로젝트 상태 업데이트
    public void updateStatus() {
        // 주최자의 상태에 따라 프로젝트 상태 업데이트
        UserProjectEntity ownerProject = userProjects.stream()
                .filter(userProject -> userProject.getUser().getUserNick().equals(this.createdBy))
                .findFirst()
                .orElse(null);

        if (ownerProject != null) {
            String ownerStatus = ownerProject.getStatus();
            if ("진행중".equals(ownerStatus)) {
                this.status = "진행중";
            } else if ("완료".equals(ownerStatus)) {
                this.status = "완료";
            }
        }
    }

    public void calculateDDay() {
        if (this.recruitmentEndDate != null) {
            this.dDay = Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), this.recruitmentEndDate));
        } else {
            this.dDay = 0;
        }
    }
    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        calculateDDay();  // ✅ D-Day 수동 계산 호출
    }

}


