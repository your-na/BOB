package com.example.bob.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 기업이 생성한 이력서 양식 엔티티 (전체 이력서 단위)
 */
@Entity
public class CoResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 이력서 ID (자동 생성)

    private String title;  // 이력서 제목 (예: 개발자 채용 양식)

    // 이력서 항목들과의 1:N 관계
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoResumeSectionEntity> sections = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * section을 추가할 때 자동으로 resume 설정까지 같이 해줌
     */
    public void addSection(CoResumeSectionEntity section) {
        sections.add(section);
        section.setResume(this);
    }

    // Getter / Setter
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<CoResumeSectionEntity> getSections() { return sections; }
    public void setSections(List<CoResumeSectionEntity> sections) { this.sections = sections; }
}
