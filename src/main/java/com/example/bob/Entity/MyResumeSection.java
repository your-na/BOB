package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * 이력서의 개별 섹션 Entity (예: 학력, 경력 등)
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResumeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 섹션 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    @ToString.Exclude // ✅ 무한 참조 방지!
    private MyResume resume; // 어떤 이력서에 속하는지

    private String type; // 선택형, 서술형, 사진 첨부 등
    private String title; // 섹션 제목
    private String comment; // 설명 문구
    private String content; // 본문 입력 내용
    private boolean multiSelect; // 복수 선택 여부

    @ElementCollection
    private List<String> tags; // 사용자 입력 태그

    @ElementCollection
    private List<String> conditions; // 선택된 조건 태그들
}
