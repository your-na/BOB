package com.example.bob.Api;

import com.example.bob.DTO.BoardPostRequestDto;
import com.example.bob.Service.BoardPostService;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;






@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class BoardPostController {

    private final BoardPostService boardPostService;
    private final UserRepository userRepository;

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

    // ✨ 내가 쓴 게시글 조회 API
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(Principal principal) {

        String loginId = principal.getName();

        // loginId → userNick 변환
        UserEntity user = userRepository.findByUserIdLogin(loginId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String userNick = user.getUserNick();

        return ResponseEntity.ok(boardPostService.getMyPosts(userNick));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deletePosts(
            @RequestBody List<Long> ids,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String loginId = principal.getName();
            boardPostService.deleteMyPosts(ids, loginId);
            return ResponseEntity.ok("삭제 성공!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }







}
