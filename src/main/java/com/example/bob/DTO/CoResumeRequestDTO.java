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

    // Lombok이 @Getter, @Setter, @AllArgsConstructor로 기본적으로 처리
}
