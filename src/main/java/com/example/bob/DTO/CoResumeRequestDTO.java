package com.example.bob.DTO;

import java.util.List;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CoResumeRequestDTO {

    private String title;                          // 이력서 제목
    private List<CoResumeSectionRequestDTO> sections;  // 섹션 목록
    private Date createdAt;                        // 작성일
    private List<String> jobTags;                  // 희망직무 태그 필드 추가

    // Lombok이 @Getter, @Setter, @AllArgsConstructor로 기본적으로 처리
}
