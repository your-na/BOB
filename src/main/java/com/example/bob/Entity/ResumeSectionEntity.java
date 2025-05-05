package com.example.bob.Entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class ResumeSectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 기업의 section을 기반으로 작성했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_section_id")
    private CoResumeSectionEntity coSection;

    // 어떤 사용자 이력서에 속해있는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private ResumeEntity resume;

    // 실제 사용자가 입력한 텍스트
    @Lob
    private String content;

    // 선택형 태그 선택 시
    @ElementCollection
    private List<String> selectedTags;

    // Getter / Setter
    public Long getId() { return id; }

    public CoResumeSectionEntity getCoSection() { return coSection; }
    public void setCoSection(CoResumeSectionEntity coSection) { this.coSection = coSection; }

    public ResumeEntity getResume() { return resume; }
    public void setResume(ResumeEntity resume) { this.resume = resume; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getSelectedTags() { return selectedTags; }
    public void setSelectedTags(List<String> selectedTags) { this.selectedTags = selectedTags; }
}
