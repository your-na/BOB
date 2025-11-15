package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardCommentResponseDto {
    private Long id;
    private String writer;     // 작성자 닉네임
    private String content;    // 댓글 본문
    private String createdAt;  // 작성일 (String 형태)
    boolean likedByCurrentUser; // ✅ 로그인 유저가 좋아요 했는지 여부
    private int likeCount;


}
