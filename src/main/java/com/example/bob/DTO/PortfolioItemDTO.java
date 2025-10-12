package com.example.bob.DTO;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioItemDTO {
    // PROJECT 또는 CONTEST
    private String type;

    // 공통 표시 정보
    private String title;        // 프로젝트/공모전명
    private String description;  // (있으면) 설명/비고
    private String status;       // 완료/수상 등
    private String filePath;     // 파일 경로


    private LocalDate startDate; // yyyy-MM-01 형태로 매핑됨
    private LocalDate endDate;   // yyyy-MM-01 형태로 매핑됨
}
