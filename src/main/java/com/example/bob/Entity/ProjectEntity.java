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

    @Column(length = 255, nullable = false)
    private String createdBy; // ✅ 닉네임 저장 (created_by 컬럼과 매핑)

    @Column(length = 255, nullable = false)
    private String creatorNick; // ✅ creator_nick 컬럼 추가

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
    private int likes; // 좋아요 개수

    @Column(length = 50, nullable = false)
    private String status; // 모집 상태 (예: 모집중)

    @Column(length = 500)
    private String description; // 설명

    @Transient // 데이터베이스에 저장되지 않음 (조회용)
    private String dDay;

    @ElementCollection
    private List<Long> likedUsers = new ArrayList<>(); // 좋아요 누른 유저들

    public void setDDay(String dDay) {
        this.dDay = dDay;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdBy == null || this.createdBy.isEmpty()) {
            this.createdBy = "default_nick"; // 기본값 설정
        }
        if (this.creatorNick == null || this.creatorNick.isEmpty()) {
            this.creatorNick = this.createdBy; // creatorNick이 없으면 createdBy 값으로 설정
        }
    }

    // 좋아요 수 증가 처리
    public void toggleLike() {
        this.likes += 1; // 좋아요 수 증가
    }

    // 좋아요 수 감소 처리
    public void decrementLike() {
        this.likes -= 1; // 좋아요 수 감소
    }

    // 특정 사용자가 좋아요를 눌렀는지 확인
    public boolean isUserLiked(Long userId) {
        return likedUsers.contains(userId); // 사용자가 좋아요를 눌렀는지 확인
    }

    // 사용자가 좋아요를 눌렀을 때 추가
    public void addUserToLikes(Long userId) {
        likedUsers.add(userId); // 사용자 추가
    }

    // 사용자가 좋아요를 취소할 때 제거
    public void removeUserFromLikes(Long userId) {
        likedUsers.remove(userId); // 사용자 제거
    }
}
