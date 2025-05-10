package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestRecruitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모집글 제목
    private String title;

    // 모집글 상세 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 모집 기간
    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;

    // 진행 기간
    private LocalDate startDate;
    private LocalDate endDate;

    // 모집 인원
    private int recruitCount;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity writer;

    // 어떤 공모전인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private ContestEntity contest;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
