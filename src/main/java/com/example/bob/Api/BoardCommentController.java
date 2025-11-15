package com.example.bob.Api;

import com.example.bob.DTO.BoardCommentRequestDto;
import com.example.bob.DTO.BoardCommentResponseDto;
import com.example.bob.Service.BoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class BoardCommentController {

    private final BoardCommentService boardCommentService;

    // 댓글 저장
    @PostMapping
    public ResponseEntity<?> saveComment(@RequestBody BoardCommentRequestDto dto) {
        try {
            boardCommentService.saveComment(dto);
            return ResponseEntity.ok("댓글이 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 등록 실패: " + e.getMessage());
        }
    }

    // 댓글 목록 조회
    @GetMapping("/{postId}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<BoardCommentResponseDto> comments =
                    boardCommentService.getCommentsByPostId(postId); // ← DTO 반환
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 조회 실패: " + e.getMessage());
        }
    }
}
