package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResumeSubmitRequestDTO {

    private Long coResumeId;                     // 어떤 기업 이력서 양식 기반인지
    private Long jobPostId;                      // 어떤 공고에 지원하는 이력서인지 (nullable 가능)
    private List<ResumeSectionSubmitDTO> sections;  // 사용자 작성 섹션 목록
}
