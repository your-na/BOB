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
    @Builder.Default
    private List<Long> likedUsers = new ArrayList<>(); // 좋아요 누른 유저들

    @Column(nullable = false)
    private int currentParticipants; // 실제 참여 인원

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserProjectEntity> userProjects = new ArrayList<>();  // ✅ 초기화 추가

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectHistoryEntity> projectHistoryEntities = new ArrayList<>();


    // ✅ 주최자가 제출을 하면 수락된 상태의 팀원들만 `"완료"`로 변경
    public void completeProject() {
        // 주최자가 파일을 제출한 경우만 프로젝트 상태 변경
        if (userProjects != null) {
            for (UserProjectEntity userProject : userProjects) {
                // 주최자가 파일을 제출한 경우만 프로젝트 상태 변경
                if (userProject.getUser().getUserNick().equals(this.createdBy) && userProject.getSubmittedFileName() != null) {
                    this.status = "완료"; // 프로젝트 상태 변경

                    // 수락된 모든 팀원들의 상태를 "완료"로 변경
                    for (UserProjectEntity member : userProjects) {
                        // "신청중", "진행중" 상태인 팀원들만 "완료"로 변경
                        if (member.getStatus().equals("진행중") || member.getStatus().equals("신청중")) {
                            member.setStatus("완료"); // 상태를 "완료"로 변경
                        }
                    }
                    break; // 주최자가 완료로 변경되면 바로 상태 변경을 완료
                }
            }
        }

        // 프로젝트 테이블 상태 업데이트는 서비스 레이어에서 처리
        // projectRepository.save(this); // 이 부분을 서비스 레이어로 이동
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


