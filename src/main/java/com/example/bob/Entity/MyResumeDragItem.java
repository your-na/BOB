package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 나만의 이력서의 드래그 항목 Entity (ex. 프로젝트, 경력 등)
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResumeDragItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayText;

    private LocalDate startDate;

    private LocalDate endDate;

    private String filePath;
    private String workplace;   // 근무지
    private String jobTitle;    // 직무
    private String status;      // 상태 (재직 중 / 퇴사 등)




    // 🔗 ManyToOne (MyResumeSection과 연관)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private MyResumeSection section;
}
