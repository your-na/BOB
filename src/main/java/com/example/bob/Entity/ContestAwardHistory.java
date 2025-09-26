package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_award_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestAwardHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;      // 참여중 / 참여완료
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;       // 공모전 이름
    private String grade;       // 수상 등급
    private String organizer;   // 주최기관

    @Lob
    private String ocrRawText;  // OCR 원문 (선택적으로 저장)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String source;

    @Column(nullable = true)
    private Long teamId;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
