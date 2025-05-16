package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // 👈 전체 생성자 자동 생성
@NoArgsConstructor  // 👈 기본 생성자도 생성
public class ApplicantDTO {
    private String userName;   // 🙋‍♀️ 지원자 이름
    private String appliedAt;  // 🕒 지원 일시 (yyyy-MM-dd HH:mm)
    private Long resumeId;     // 📄 이력서 ID (resume/detail 링크용)
}
