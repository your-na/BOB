package com.example.bob.DTO;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplyRequestDTO {
    private Long jobId;           // 지원할 공고 ID
    private String myResumeTitle; // 사용자가 선택한 이력서 제목
}
