package com.example.bob.DTO;

import java.util.List;

public class ResumeSectionDTO {
    private Long id;
    private String title;
    private String comment;
    private String type;
    private boolean multiSelect;
    private List<String> tags;        // 선택지로 쓸 태그
    private List<String> conditions;

    // Getter / Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }// 조건: 500자 이상, 상태입력 등


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    private String content;
    private List<String> selectedTags;
    private List<EducationDTO> educations;
    private List<String> fileNames;
    private List<ResumeDragItemDTO> dragItems;

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getSelectedTags() {
        return selectedTags;
    }
    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags;
    }

    public List<EducationDTO> getEducations() {
        return educations;
    }
    public void setEducations(List<EducationDTO> educations) {
        this.educations = educations;
    }

    public List<String> getFileNames() {
        return fileNames;
    }
    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<ResumeDragItemDTO> getDragItems() {
        return dragItems;
    }
    public void setDragItems(List<ResumeDragItemDTO> dragItems) {
        this.dragItems = dragItems;
    }

}
