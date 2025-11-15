package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardCommentResponseDto {
    private String writer;     // 작성자 닉네임
    private String content;    // 댓글 본문
    private String createdAt;  // 작성일 (String 형태)
}
