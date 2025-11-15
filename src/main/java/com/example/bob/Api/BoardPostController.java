package com.example.bob.Api;

import com.example.bob.DTO.BoardPostRequestDto;
import com.example.bob.Service.BoardPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class BoardPostController {

    private final BoardPostService boardPostService;

    // 게시글 작성 API
    @PostMapping
    public ResponseEntity<String> createPost(@ModelAttribute BoardPostRequestDto dto) {
        try {
            boardPostService.savePost(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("카테고리 값이 잘못되었습니다.");
        } catch (MultipartException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 업로드 실패");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }

    // 게시글 목록 조회 API
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            return ResponseEntity.ok(boardPostService.getAllPosts());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 목록 조회 중 오류 발생");
        }
    }

}
