package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 원본 공모전
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private ContestEntity contest;

    private String title;
    private String organizer;
    private String category;
    private String target;
    private String region;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate judgeStartDate;
    private LocalDate judgeEndDate;

    @Lob
    private String awardDetails;

    private String applicationMethod;

    @Lob
    private String description;

    private String imageUrl;
    private String status;
    private String creatorType;
    private boolean isApproved;
    private boolean isOnlyBOB;

    private LocalDateTime createdAt;

    public static ContestHistoryEntity fromEntity(ContestEntity contest) {
        return ContestHistoryEntity.builder()
                .contest(contest)
                .title(contest.getTitle())
                .organizer(contest.getOrganizer())
                .category(contest.getCategory())
                .target(contest.getTarget())
                .region(contest.getRegion())
                .startDate(contest.getStartDate())
                .endDate(contest.getEndDate())
                .judgeStartDate(contest.getJudgeStartDate())
                .judgeEndDate(contest.getJudgeEndDate())
                .awardDetails(contest.getAwardDetails())
                .applicationMethod(contest.getApplicationMethod())
                .description(contest.getDescription())
                .imageUrl(contest.getImageUrl())
                .status(contest.getStatus())
                .creatorType(contest.getCreatorType())
                .isApproved(contest.isApproved())
                .isOnlyBOB(contest.isOnlyBOB())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
