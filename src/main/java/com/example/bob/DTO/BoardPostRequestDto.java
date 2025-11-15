package com.example.bob.DTO;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostRequestDto {

    private String category;     // 카테고리명 (예: "공모전")
    private String title;        // 게시글 제목
    private String content;      // 게시글 본문
    private String writer;       // 작성자명
    private MultipartFile file;  // 첨부파일 (선택)
}
