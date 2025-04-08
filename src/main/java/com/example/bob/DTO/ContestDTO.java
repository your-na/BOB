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
    private String imageUrl;

    private LocalDate startDate;
    private LocalDate endDate;

    private String judge;

    private String awardDetails;
    private String applicationMethod;
    private String description;
    private String status;


    private long remainingDays; // D-day 계산용

    private String creatorType; // "ADMIN", "COMPANY"
    private boolean isApproved;
    private boolean isOnlyBOB;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setIsApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

    public void setIsOnlyBOB(boolean isOnlyBOB) {
        this.isOnlyBOB = isOnlyBOB;
    }

    public ContestEntity toEntity() {
        return ContestEntity.builder()
                .id(id)
                .title(title)
                .organizer(organizer)
                .category(category)
                .target(target)
                .region(region)
                .imageUrl(imageUrl)
                .startDate(startDate)
                .endDate(endDate)
                .judge(judge)
                .awardDetails(awardDetails)
                .applicationMethod(applicationMethod)
                .description(description)
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
        long daysLeft = 0;
        if (entity.getEndDate() != null) {
            daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), entity.getEndDate()) + 1;
        }

        return ContestDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .organizer(entity.getOrganizer())
                .category(entity.getCategory())
                .target(entity.getTarget())
                .region(entity.getRegion())
                .imageUrl(entity.getImageUrl())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .judge(entity.getJudge())
                .awardDetails(entity.getAwardDetails())
                .applicationMethod(entity.getApplicationMethod())
                .description(entity.getDescription())
                .creatorType(entity.getCreatorType())
                .isOnlyBOB(entity.isOnlyBOB())
                .isApproved(entity.isApproved())
                .status(entity.getStatus())
                .remainingDays(daysLeft)
                .build();
    }
}
