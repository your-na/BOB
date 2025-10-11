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

    // ✅ 어떤 이력서 섹션에 속하는지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_section_id")
        private ResumeSectionEntity resumeSection;

    private String type;         // 프로젝트 or 공모전
    private String status;       // 진행 상태 (진행중, 완료 등)
    private String startYear;    // 시작년도
    private String startMonth;   // 시작월
    private String endYear;      // 종료년도
    private String endMonth;     // 종료월
    private String title;        // 프로젝트/공모전 이름
    private String submittedFile; // 제출파일 이름 (파일명)
}
