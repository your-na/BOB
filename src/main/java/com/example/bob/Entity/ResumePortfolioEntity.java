package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ResumePortfolioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_section_id")
    private ResumeSectionEntity resumeSection;

    private String type;          // PROJECT / CONTEST
    private String status;        // 진행 상태 (진행중, 완료 등)
    private String startYear;     // 시작년도
    private String startMonth;    // 시작월
    private String endYear;       // 종료년도
    private String endMonth;      // 종료월
    private String title;         // 프로젝트/공모전 이름
    private String submittedFile; // 제출파일 이름

    // ✅ 새로 추가
    @Column(columnDefinition = "TEXT")
    private String description;   // 프로젝트 설명

    // ✅ 새로 추가
    private String filePath;      // 업로드된 파일 경로 (/uploads/projectFiles/abc.pdf)
}
