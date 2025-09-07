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
}
