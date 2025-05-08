package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사진 또는 파일 첨부 데이터를 저장하는 엔티티.
 * 하나의 ResumeSectionEntity(파일/사진 첨부 섹션)에 여러 파일이 연결될 수 있음.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ResumeFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연결된 이력서 섹션 (파일/사진 첨부 섹션 중 하나)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_section_id")
    private ResumeSectionEntity resumeSection;

    // 실제 저장된 파일 이름 (서버에 저장된 파일 경로 또는 UUID 이름 등)
    private String fileName;
}
