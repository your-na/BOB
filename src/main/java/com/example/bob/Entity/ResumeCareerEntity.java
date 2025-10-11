package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ResumeCareerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 어떤 섹션(경력 섹션)에 속하는지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_section_id")
    private ResumeSectionEntity resumeSection;

    private String companyName;  // 회사명
    private String position;     // 직무명
    private String status;       // 상태 (재직/퇴사 등)
    private String startYear;    // 시작년도
    private String startMonth;   // 시작월
    private String endYear;      // 종료년도
    private String endMonth;     // 종료월
}
