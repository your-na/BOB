package com.example.bob.DTO;

import com.example.bob.Entity.ContestAwardHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestAwardHistoryResponseDTO {
    private Long id;
    private String title;
    private String organizer;
    private String grade;
    private String status;
    private String source;
    private String ocrRawText;
    private LocalDate startDate;
    private LocalDate endDate;

    private Long userId; // 작성자 ID만 응답

    // ✅ Entity → DTO 변환 편의 메서드
    public static ContestAwardHistoryResponseDTO fromEntity(ContestAwardHistory entity) {
        return ContestAwardHistoryResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .organizer(entity.getOrganizer())
                .grade(entity.getGrade())
                .status(entity.getStatus())
                .source(entity.getSource())
                .ocrRawText(entity.getOcrRawText())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .userId(entity.getUser().getId()) // 유저 전체 말고 ID만
                .build();
    }
}
