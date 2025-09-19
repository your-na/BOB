package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestAwardHistoryRequestDTO {
    private String grade;
    private String organizer;
    private String source;
    private Long teamId;
    private String ocrRawText;
}

