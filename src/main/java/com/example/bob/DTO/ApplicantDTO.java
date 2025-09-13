package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicantDTO {
    private String userName;   // 지원자 이름
    private String appliedAt;  // 지원 일시
    private Long resumeId;     // 기업 이력서 ID
    private Long myResumeId;   // 내가 만든 이력서 ID
}
