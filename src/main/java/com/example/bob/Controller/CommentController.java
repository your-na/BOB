package com.example.bob.Controller;

import com.example.bob.Entity.CommentEntity;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.CommentRepository;
import com.example.bob.Service.ProjectService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
public class CommentController {

    private final CommentRepository commentRepository;
    private final ProjectService projectService;

    /** ✅ 댓글 등록 API */
    @PostMapping("/{projectId}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long projectId,
                                        @RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String content = body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ 댓글 내용이 비어있습니다.");
        }

        UserEntity user = userDetails.getUserEntity();
        ProjectEntity project = projectService.getProjectById(projectId);

        CommentEntity comment = CommentEntity.builder()
                .content(content)
                .user(user)
                .project(project)
                .build();

        commentRepository.save(comment);

        Map<String, Object> response = new HashMap<>();
        response.put("writer", user.getUserNick());
        response.put("content", content);
        response.put("createdAt", comment.getCreatedAt().toString());

        return ResponseEntity.ok(response);
    }

    /** ✅ 댓글 목록 조회 API */
    @GetMapping("/{projectId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long projectId) {
        List<CommentEntity> comments = commentRepository.findByProject_IdOrderByCreatedAtAsc(projectId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CommentEntity c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("writer", c.getUser().getUserNick());
            map.put("content", c.getContent());
            map.put("createdAt", c.getCreatedAt().toString());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}
