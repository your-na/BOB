package com.example.bob.Entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class CoResumeSectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String title;
    private String comment;
    private String content;

    private boolean multiSelect;

    private String directInputValue;

    @ElementCollection
    private List<String> conditions;  // 조건들 (예: 50자 이상 등)

    /**
     * 이 섹션에 속한 선택형 보기 태그들 (예: 백엔드, 프론트엔드)
     */
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoResumeTagEntity> sectionTags = new ArrayList<>();

    /**
     * 태그 문자열 리스트 - 실제 DB에는 저장하지 않지만, DTO 변환용으로 사용
     */
    @Transient
    private List<String> tags;

    /**
     * 이 섹션이 소속된 이력서
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private CoResumeEntity resume;

    // ✅ Getter / Setter

    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isMultiSelect() { return multiSelect; }
    public void setMultiSelect(boolean multiSelect) { this.multiSelect = multiSelect; }

    public String getDirectInputValue() { return directInputValue; }
    public void setDirectInputValue(String directInputValue) { this.directInputValue = directInputValue; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    public List<CoResumeTagEntity> getSectionTags() { return sectionTags; }
    public void setSectionTags(List<CoResumeTagEntity> sectionTags) { this.sectionTags = sectionTags; }

    public CoResumeEntity getResume() { return resume; }
    public void setResume(CoResumeEntity resume) { this.resume = resume; }

    // ✅ tags 필드 (Transient)
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
