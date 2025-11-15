package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardPostDetailDto {
    private Long id;
    private String category;
    private String title;
    private String content;
    private String writer;
    private String createdAt;
    private String filePath; // 이미지 또는 파일 경로
}
