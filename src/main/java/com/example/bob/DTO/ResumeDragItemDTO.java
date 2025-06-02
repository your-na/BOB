package com.example.bob.DTO;

import java.util.List;

public class ResumeDragItemDTO {

    private Long coSectionId;     // 섹션 ID (프론트에서 전달)
    private String itemType;      // "PROJECT", "CONTEST", "LICENSE", "JOB"
    private Long referenceId;     // 참조 ID
    private String displayText;   // 표시할 텍스트
    private String filePath;      // 파일 경로 (선택)


    public ResumeDragItemDTO() {}

    public ResumeDragItemDTO(Long coSectionId, String itemType, Long referenceId, String displayText, String filePath) {
        this.coSectionId = coSectionId;
        this.itemType = itemType;
        this.referenceId = referenceId;
        this.displayText = displayText;
        this.filePath = filePath;
    }

    public Long getCoSectionId() {
        return coSectionId;
    }

    public void setCoSectionId(Long coSectionId) {
        this.coSectionId = coSectionId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private String startDate;
    private String endDate;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
