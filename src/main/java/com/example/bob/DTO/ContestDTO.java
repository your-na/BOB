package com.example.bob.DTO;

import com.example.bob.Entity.ContestEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestDTO {

    private Long id;
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

    private long remainingDays; // D-day 계산용

    private String creatorType; // "ADMIN", "COMPANY"
    private boolean isApproved;
    private boolean isOnlyBOB;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContestEntity toEntity() {
        return ContestEntity.builder()
                .id(id)
                .title(title)
                .organizer(organizer)
                .category(category)
                .target(target)
                .region(region)
                .startDate(startDate)
                .endDate(endDate)
                .judgeStartDate(judgeStartDate)
                .judgeEndDate(judgeEndDate)
                .awardDetails(awardDetails)
                .applicationMethod(applicationMethod)
                .description(description)
                .imageUrl(imageUrl)
                .status(status)
                .creatorType(creatorType)
                .isApproved(isApproved)
                .isOnlyBOB(isOnlyBOB)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static ContestDTO fromEntity(ContestEntity entity)
    {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), entity.getStartDate());

        return ContestDTO.builder()
                .id(entity.getId())
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
                .creatorType(entity.getCreatorType())
                .isOnlyBOB(entity.isOnlyBOB())
                .isApproved(entity.isApproved())
                .status(entity.getStatus())
                .remainingDays(daysLeft)
                .build();
    }
}
