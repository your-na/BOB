package com.example.bob.Entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * 기업이 설정한 이력서 양식 내 개별 항목 섹션 엔티티
 */
@Entity
public class CoResumeSectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 섹션 ID

    private String type;     // 섹션 유형 (예: 선택형, 서술형 등)
    private String title;    // 섹션 제목 (예: 학력사항, 희망직무 등)
    private String comment;  // 항목 설명/가이드 (예: 학력은 최근 순으로 입력하세요)
    private String content;  // 기본값 또는 설명 텍스트 (서술형에 해당)

    /**
     * 선택형일 경우 사용할 태그 목록
     * 예: ["백엔드", "프론트엔드", "AI"]
     */
    @ElementCollection
    private List<String> tags;  // 선택형 태그 목록

    /**
     * 선택된 조건 항목들 (예: "50자 이상", "200자 이상")
     */
    @ElementCollection
    private List<String> conditions;  // 조건 항목들 (예: "50자 이상", "200자 이상")

    private boolean multiSelect; // 복수선택 여부

    private String directInputValue; // 직접 입력 값 (사용자가 입력한 값)

    /**
     * 이 섹션이 소속된 이력서 양식 (N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private CoResumeEntity resume;

    // Getter / Setter
    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    public boolean isMultiSelect() { return multiSelect; }
    public void setMultiSelect(boolean multiSelect) { this.multiSelect = multiSelect; }

    public String getDirectInputValue() { return directInputValue; }
    public void setDirectInputValue(String directInputValue) { this.directInputValue = directInputValue; }

    public CoResumeEntity getResume() { return resume; }
    public void setResume(CoResumeEntity resume) { this.resume = resume; }
}

