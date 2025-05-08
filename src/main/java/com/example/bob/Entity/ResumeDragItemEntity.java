package com.example.bob.Entity;

import jakarta.persistence.*;

@Entity
public class ResumeDragItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 섹션과 다대일 관계로 변경 (ResumeSectionEntity 기준)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private ResumeSectionEntity section;

    private String itemType;     // "PROJECT", "CONTEST", "LICENSE", "JOB_HISTORY" 등
    private Long referenceId;    // 실제 항목의 ID
    private String displayText;  // 예: 공모전명, 자격증 이름
    private String filePath;     // 관련 파일이 있을 경우

    // 기본 생성자
    public ResumeDragItemEntity() {
    }

    // 전체 필드 생성자
    public ResumeDragItemEntity(ResumeSectionEntity section, String itemType, Long referenceId, String displayText, String filePath) {
        this.section = section;
        this.itemType = itemType;
        this.referenceId = referenceId;
        this.displayText = displayText;
        this.filePath = filePath;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public ResumeSectionEntity getSection() {
        return section;
    }

    public void setSection(ResumeSectionEntity section) {
        this.section = section;
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
}
