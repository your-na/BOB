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

    private int likeCount;      // ❤️ 좋아요 총 개수
    private boolean likedByMe;  // 💗 내가 좋아요 눌렀는지 여부
}
