package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * 나만의 이력서 Entity (제목, 작성자, 섹션 목록 포함)
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 이력서 고유 ID

    private String title; // 이력서 제목

    private String memberId; // 사용자 아이디 (user_id_login 저장됨)

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MyResumeSection> sections = new HashSet<>();

    @CreationTimestamp // INSERT 시 자동 생성됨
    private LocalDateTime createdAt;

    // --- 사용자 정보 (상세보기 전용) ---
    @Transient
    private String userName;

    @Transient
    private String profileImageUrl;

    @Transient
    private String mainLanguage;

    @Transient
    private String sex;

    @Transient
    private String birthday;

    @Transient
    private Integer age;

    @Transient
    private String userPhone;

    @Transient
    private String userEmail;

    @Transient
    private String region;

    // 섹션 추가 시 양방향 연관관계 세팅
    public void addSection(MyResumeSection section) {
        section.setResume(this);
        this.sections.add(section);
    }
}
