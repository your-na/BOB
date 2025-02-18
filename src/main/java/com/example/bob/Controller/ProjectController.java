package com.example.bob.Controller;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * ✅ 프로젝트 목록 페이지로 이동
     */
    @GetMapping("/project")
    public String showProjects(Model model) {
        List<ProjectEntity> projects = projectService.getAllProjects(); // DB에서 모든 프로젝트 가져오기
        model.addAttribute("projects", projects);
        return "project"; // 프로젝트 목록 페이지
    }

    /**
     * ✅ 프로젝트 상세 보기 (postproject.html)
     */
    @GetMapping("/postproject/{id}")
    public String showProjectDetail(@PathVariable Long id, Model model) {
        ProjectEntity project = projectService.getProjectById(id);

        // 조회수 증가 처리
        project = projectService.incrementViews(id); // 조회수 증가

        model.addAttribute("project", project);
        return "postproject";  // 프로젝트 상세 페이지
    }

    @PostMapping("/postproject/{id}/like")
    @ResponseBody
    public ResponseEntity<?> likeProject(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 좋아요 토글 메서드 호출 (좋아요 수 및 리스트 갱신)
            ProjectEntity updatedProject = projectService.toggleLike(id, userId);
            return ResponseEntity.ok(updatedProject.getLikes()); // 변경된 좋아요 개수 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("좋아요 요청 실패");
        }
    }

    /**
     * ✅ 스크랩 API (AJAX 요청)
     */
    @PostMapping("/postproject/{id}/scrap")
    @ResponseBody
    public ResponseEntity<?> scrapProject(@PathVariable Long id, @RequestParam Long userId) {
        try {
            ProjectEntity updatedProject = projectService.toggleScrap(id, userId);
            return ResponseEntity.ok("스크랩 처리 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("스크랩 요청 실패");
        }
    }

    // 조회수 증가를 위한 새로운 엔드포인트 추가
    @PostMapping("/postproject/{id}/incrementViews")
    @ResponseBody
    public ResponseEntity<?> incrementViews(@PathVariable Long id) {
        try {
            projectService.incrementViews(id); // 조회수 증가
            return ResponseEntity.ok("조회수 증가 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회수 증가 실패");
        }
    }
}
