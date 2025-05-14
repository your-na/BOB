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
    private String fileName;                             // 첨부 파일명
    private List<ResumeDragItemDTO> dragItems;           // 드래그 항목들

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

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<ResumeDragItemDTO> getDragItems() { return dragItems; }
    public void setDragItems(List<ResumeDragItemDTO> dragItems) { this.dragItems = dragItems; }
}
