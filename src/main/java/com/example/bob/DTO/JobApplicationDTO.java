package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDTO {

    private String appliedDate;     // 신청 날짜 (yyyy-MM-dd 등)
    private String jobTitle;        // 공고 제목
    private String companyIntro;    // 회사 소개글
    private String status;          // 상태
    private Long jobPostId;        // 공고 id
    private Long resumeId;      // 기업용일 경우만 값 존재
    private Long myResumeId;    // 회원용일 경우만 값 존재
    private Long applicationId;

}
