package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ResumeEducationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학력사항이 속한 섹션과 연결 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_section_id")
    private ResumeSectionEntity resumeSection;

    private String schoolName;  // 학교명
    private String majorName;   // 학과명
    private String status;      // 상태 (재학 / 졸업)
    private String startYear;   // 입학 연도
    private String startMonth;  // 입학 월
    private String endYear;     // 졸업 연도 (재학이면 null 가능)
    private String endMonth;    // 졸업 월 (재학이면 null 가능)
}
