package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

/**
 * 학력사항에 대한 개별 항목 DTO.
 */
@Getter
@Setter
public class EducationDTO {
    private String schoolName;  // 학교명
    private String majorName;   // 학과명
    private String status;      // 재학/졸업 상태
    private String startYear;   // 입학 연도
    private String startMonth;  // 입학 월
    private String endYear;     // 졸업 연도 (재학이면 null 가능)
    private String endMonth;    // 졸업 월 (재학이면 null 가능)
}
