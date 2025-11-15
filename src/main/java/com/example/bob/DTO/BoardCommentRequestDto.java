package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCommentRequestDto {

    private Long postId;     // 댓글이 달릴 게시글 ID
    private String content;  // 댓글 내용

    // writer는 로그인된 사용자 정보로 서버에서 처리하므로 입력받지 않음
}
