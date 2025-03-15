package com.example.bob.DTO;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {

    private Long id; // 프로젝트 ID
    private String title; // 프로젝트명
    private String createdBy; // 생성자
    private String description; // 프로젝트 내용
    private String goal; // 프로젝트 목표
    private LocalDate startDate; // 시작 날짜
    private LocalDate endDate; // 종료 날짜
    private int recruitmentCount; // 모집 인원
    private int currentParticipants; // 실제 참여 인원
    private int views; // 조회수
    private int likes; // 좋아요 개수
    private String status; // 모집 상태
    private int recruitmentPeriod; // 모집 기간 필드 추가
    private LocalDate recruitmentEndDate;
    private LocalDate recruitmentStartDate;

    // 생성된 프로젝트 상태 표시 메서드
    public String getParticipantsStatus() {
        return currentParticipants + "/" + recruitmentCount;
    }
}
