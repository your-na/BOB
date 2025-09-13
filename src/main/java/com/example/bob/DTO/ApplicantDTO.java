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
    private Long resumeId;     // 이력서 ID (resume/detail 링크용)

    // 커스텀 생성자: DB에서 받아온 값으로 resumeId 선택
    public ApplicantDTO(String userName, String appliedAt, Long resumeIdFromCompany, Long myResumeId) {
        this.userName = userName;
        this.appliedAt = appliedAt;
        this.resumeId = (resumeIdFromCompany != null) ? resumeIdFromCompany : myResumeId;
    }
}
