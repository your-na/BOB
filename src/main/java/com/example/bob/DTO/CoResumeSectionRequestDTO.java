package com.example.bob.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
    public class CoResumeSectionRequestDTO {

    private String type;
    private String title;
    private String comment;
    private String content;  // 서술형일 때 사용
    private List<String> tags;  // 선택형일 때 사용

    // 기본 생성자 Lombok이 제공해줌
}
