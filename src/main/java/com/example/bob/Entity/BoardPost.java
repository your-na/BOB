package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity // JPA 엔티티 선언
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPost {

    @Id // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    private Long id;

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    @Column(nullable = false)
    private BoardCategory category; // 게시판 카테고리

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 내용

    @Column(nullable = false)
    private String writer; // 작성자

    private String filePath; // 파일 경로 (선택)

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt; // 작성일시

    private int likeCount = 0;  // ❤️ 좋아요 개수 저장


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // 저장 전 자동 설정
    }
}
