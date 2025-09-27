package com.example.bob.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResumeDragItemDto {

    private String displayText;

    private String startDate;

    private String endDate;

    private String filePath;

    private String workplace;
    private String jobTitle;
    private String status;
    private Integer periodMonths; // ✅ 근속 개월 수



}
