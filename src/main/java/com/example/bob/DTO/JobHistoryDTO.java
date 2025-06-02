package com.example.bob.DTO;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ 이게 필요합니다
public class JobHistoryDTO {

    private Long id;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String workplace;
    private String jobTitle;
}
