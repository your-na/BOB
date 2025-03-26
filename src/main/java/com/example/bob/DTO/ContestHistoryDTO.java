package com.example.bob.DTO;

import com.example.bob.Entity.ContestHistoryEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestHistoryDTO {

    private Long id;
    private Long contestId;  // 원본 공모전 ID

    private String title;
    private String organizer;
    private String category;
    private String target;
    private String region;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate judgeStartDate;
    private LocalDate judgeEndDate;

    private String awardDetails;
    private String applicationMethod;
    private String description;
    private String imageUrl;
    private String status;

    private String creatorType;
    private boolean isApproved;
    private boolean isOnlyBOB;

    private LocalDateTime createdAt;

    public static ContestHistoryDTO fromEntity(ContestHistoryEntity entity) {
        return ContestHistoryDTO.builder()
                .id(entity.getId())
                .contestId(entity.getContest() != null ? entity.getContest().getId() : null)
                .title(entity.getTitle())
                .organizer(entity.getOrganizer())
                .category(entity.getCategory())
                .target(entity.getTarget())
                .region(entity.getRegion())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .judgeStartDate(entity.getJudgeStartDate())
                .judgeEndDate(entity.getJudgeEndDate())
                .awardDetails(entity.getAwardDetails())
                .applicationMethod(entity.getApplicationMethod())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus())
                .creatorType(entity.getCreatorType())
                .isApproved(entity.isApproved())
                .isOnlyBOB(entity.isOnlyBOB())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
