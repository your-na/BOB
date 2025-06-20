package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * ✅ resumehistory에서 사용하는 간단 학력 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EducationSimpleDTO {
    private String schoolName;  // 학교명
    private String majorName;   // ✅ 학과명 추가
    private String status;      // 재학 / 졸업
    private String startDate;   // yyyy-MM-dd 형식
    private String endDate;     // yyyy-MM-dd 형식
}
