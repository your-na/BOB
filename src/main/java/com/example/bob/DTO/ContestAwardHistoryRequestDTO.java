package com.example.bob.DTO;

import com.example.bob.Entity.ContestAwardHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestAwardHistoryRequestDTO {
    private String grade;
    private String organizer;
    private String source;
    private Long teamId;
    private String ocrRawText;
    private String title;
    private String status;

    private String startDate;
    private String endDate;

}

