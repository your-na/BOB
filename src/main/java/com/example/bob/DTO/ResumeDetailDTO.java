package com.example.bob.DTO;

import java.util.List;

/**
 * 이력서 전체 상세보기 DTO (조회 전용)
 */
public class ResumeDetailDTO {
    private String title;                              // 이력서 제목
    private List<String> jobTags;                      // 희망직무 태그
    private List<ResumeDetailSectionDTO> sections;     // 각 섹션 목록

    // Getter / Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getJobTags() { return jobTags; }
    public void setJobTags(List<String> jobTags) { this.jobTags = jobTags; }

    public List<ResumeDetailSectionDTO> getSections() { return sections; }
    public void setSections(List<ResumeDetailSectionDTO> sections) { this.sections = sections; }
}
