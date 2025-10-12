package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;


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

    private String type; // 선택형, 서술형, 프젝, 공모전
    private String title; // 섹션 제목
    private String comment; // 설명 문구
    private String content; // 본문 입력 내용
    private boolean multiSelect; // 복수 선택 여부

    @ElementCollection
    private List<String> tags; // 사용자 입력 태그

    @ElementCollection
    private List<String> conditions; // 선택된 조건 태그들

    @ElementCollection
    private List<String> fileNames = new ArrayList<>(); // 첨부된 파일 이름 리스트
    

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MyResumeDragItem> dragItems = new ArrayList<>();


    // 연관관계 메서드
    public void addDragItem(MyResumeDragItem item) {
        if (dragItems == null) {
            dragItems = new ArrayList<>();
        }
        dragItems.add(item);
        item.setSection(this);
    }

}
