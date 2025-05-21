package com.example.bob.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;

/**
 * 기업이 생성한 이력서 양식 엔티티 (전체 이력서 단위)
 */
@Entity
public class CoResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 이력서 ID (자동 생성)

    private String title;  // 이력서 제목 (예: 개발자 채용 양식)

    // 작성일 필드를 Date로 변경
    @CreatedDate
    @Temporal(TemporalType.DATE)
    private Date createdAt;  // 작성일 (yyyy-MM-dd 형식)

    // 이력서 항목들과의 1:N 관계, EAGER로 설정하여 sections를 즉시 로딩
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CoResumeSectionEntity> sections = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoResumeTagEntity> jobTags = new ArrayList<>();

<<<<<<< HEAD

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company;

    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }
=======
    // 📌 CoResumeEntity.java
    @ManyToOne
    @JoinColumn(name = "company_id") // foreign key
    private CompanyEntity company;


>>>>>>> develop



    /**
     * 연관관계 편의 메서드
     * section을 추가할 때 자동으로 resume 설정까지 같이 해줌
     */
    public void addSection(CoResumeSectionEntity section) {
        sections.add(section);
        section.setResume(this); // 현재 이력서 객체와 섹션을 연결
    }

    // Getter / Setter
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<CoResumeSectionEntity> getSections() { return sections; }
    public void setSections(List<CoResumeSectionEntity> sections) { this.sections = sections; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<CoResumeTagEntity> getJobTags() {
        return jobTags;
    }
    public void setJobTags(List<CoResumeTagEntity> jobTags) {
        this.jobTags = jobTags;
    }

    // ✅ 회사 getter / setter
    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }


}
