package com.example.bob.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "co_resume_tag_entity")
public class CoResumeTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tag; // 태그 텍스트

    /**
     * 어떤 섹션에 속한 태그인지 (선택형 보기용 태그일 경우)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private CoResumeSectionEntity section;

    /**
     * 이력서 전체 태그 (희망직무 태그일 경우)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private CoResumeEntity resume;

    // --- Getter/Setter ---
    public Long getId() { return id; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public CoResumeSectionEntity getSection() { return section; }
    public void setSection(CoResumeSectionEntity section) { this.section = section; }

    public CoResumeEntity getResume() { return resume; }
    public void setResume(CoResumeEntity resume) { this.resume = resume; }
}
