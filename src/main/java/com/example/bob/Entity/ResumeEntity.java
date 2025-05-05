package com.example.bob.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 기업 양식(CoResumeEntity)을 기반으로 작성한 이력서인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_resume_id")
    private CoResumeEntity coResume;

    // 사용자 정보 (유저가 있다면 연결, 없으면 삭제 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSectionEntity> sections = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedAt;

    // 편의 메서드
    public void addSection(ResumeSectionEntity section) {
        sections.add(section);
        section.setResume(this);
    }

    // Getter / Setter
    public Long getId() { return id; }

    public CoResumeEntity getCoResume() { return coResume; }
    public void setCoResume(CoResumeEntity coResume) { this.coResume = coResume; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public List<ResumeSectionEntity> getSections() { return sections; }
    public void setSections(List<ResumeSectionEntity> sections) { this.sections = sections; }

    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
}
