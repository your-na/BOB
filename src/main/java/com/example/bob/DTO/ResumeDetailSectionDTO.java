package com.example.bob.DTO;

import java.util.List;

/**
 * 이력서 상세보기에서 사용하는 개별 섹션 DTO
 */
public class ResumeDetailSectionDTO {
    private Long id;
    private String title;                  // 섹션 제목
    private String comment;                // 설명
    private String type;                   // 섹션 유형
    private String content;                // 서술형 텍스트
    private List<String> selectedTags;     // 선택형 태그
    private List<String> conditions;
    private List<String> tags;
    private List<EducationDTO> educations;               // 학력 리스트
    private List<String> fileNames;                         // 첨부 파일명
    private List<ResumeDragItemDTO> dragItems;
    private List<JobHistoryDTO> careers;  // ✅ 추가: 경력 리스트
    private List<PortfolioItemDTO> portfolios;
    // ✅ 추가: 포트폴리오 리스트

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getSelectedTags() { return selectedTags; }
    public void setSelectedTags(List<String> selectedTags) { this.selectedTags = selectedTags; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<EducationDTO> getEducations() { return educations; }
    public void setEducations(List<EducationDTO> educations) { this.educations = educations; }

    public List<String> getFileNames() {
        return fileNames;
    }
    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<ResumeDragItemDTO> getDragItems() { return dragItems; }
    public void setDragItems(List<ResumeDragItemDTO> dragItems) { this.dragItems = dragItems; }

    public List<JobHistoryDTO> getCareers() {
        return careers;
    }
    public void setCareers(List<JobHistoryDTO> careers) {
        this.careers = careers;
    }

    public List<PortfolioItemDTO> getPortfolios() {
        return portfolios;
    }
    public void setPortfolios(List<PortfolioItemDTO> portfolios) {
        this.portfolios = portfolios;
    }

}
