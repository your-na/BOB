package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyResumeApplicationRequest {
    private Long jobPostId;   // 지원한 공고 ID
    private Long myResumeId;   // 나만의 이력서 ID (MyResume의 id)
    private Long resumeId;      // 기업용일 경우만 값 존재
    private String message;   // 합격 메시지
}
