package com.example.bob.DTO;

import java.time.LocalDate;

public class ProjectDTO {

    private Long id; // 프로젝트 ID
    private String title; // 프로젝트명
    private String createdBy; // 생성자
    private LocalDate startDate; // 시작 날짜
    private LocalDate endDate; // 종료 날짜
    private int recruitmentCount; // 모집 인원
    private int currentParticipants; // 실제 참여 인원
    private int views; // 조회수
    private int likes; // 좋아요 개수
    private String status; // 모집 상태
    private int recruitmentPeriod; // 모집 기간 필드 추가

    // 생성자
    public ProjectDTO(Long id, String title, String createdBy, LocalDate startDate, LocalDate endDate,
                      int recruitmentCount, int currentParticipants, int views, int likes, String status, int recruitmentPeriod) {
        this.id = id;
        this.title = title;
        this.createdBy = createdBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitmentCount = recruitmentCount;
        this.currentParticipants = currentParticipants;
        this.views = views;
        this.likes = likes;
        this.status = status;
        this.recruitmentPeriod = recruitmentPeriod;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getRecruitmentCount() {
        return recruitmentCount;
    }

    public void setRecruitmentCount(int recruitmentCount) {
        this.recruitmentCount = recruitmentCount;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRecruitmentPeriod() {
        return recruitmentPeriod;
    }

    public void setRecruitmentPeriod(int recruitmentPeriod) {
        this.recruitmentPeriod = recruitmentPeriod;
    }

    // 생성된 프로젝트 상태 표시 메서드
    public String getParticipantsStatus() {
        return currentParticipants + "/" + recruitmentCount;
    }
}
