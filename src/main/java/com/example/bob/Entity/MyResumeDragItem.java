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

    // 🔗 ManyToOne (MyResumeSection과 연관)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private MyResumeSection section;
}
