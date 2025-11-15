package com.example.bob.Api;

import com.example.bob.DTO.BoardPostRequestDto;
import com.example.bob.Service.BoardPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import java.security.Principal;




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

    // 게시글 상세보기  API
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(
            @PathVariable Long id,
            Principal principal
    ) {
        try {
            String username = null;

            // 로그인 되어 있을 때만 username 채움
            if (principal != null) {
                username = principal.getName();  // ⭐ loginId
            }

            return ResponseEntity.ok(
                    boardPostService.getPostById(id, username)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 게시글을 찾을 수 없습니다.");
        }
    }

    // ===== 좋아요 토글 API =====
    @PostMapping("/{id}/like")
    public ResponseEntity<?> togglePostLike(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        String username = principal.getName();
        int likeCount = boardPostService.toggleLike(id, username);

        return ResponseEntity.ok(String.valueOf(likeCount));
    }




}
