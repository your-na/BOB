package com.example.bob.Controller;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Service.ProjectService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    /**
     * ✅ 좋아요 토글 API
     */
    @PostMapping("/postproject/{id}/like")
    @ResponseBody
    public ResponseEntity<?> likeProject(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 좋아요 토글 메서드 호출 (좋아요 수 및 리스트 갱신)
            ProjectEntity updatedProject = projectService.toggleLike(id, userId);

            // 변경된 좋아요 개수 반환
            return ResponseEntity.ok(updatedProject.getLikes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("좋아요 요청 실패");
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

    /**
     * ✅ 프로젝트 생성 페이지 (newproject.html)
     */
    @GetMapping("/newproject")
    public String showNewProjectForm() {
        return "newproject"; // newproject.html 반환
    }

    /**
     * ✅ 프로젝트 생성 페이지 (`/bw`에서도 접근 가능하도록 추가)
     */
    @GetMapping("/bw")
    public String showNewProjectFormViaBw() {
        return "newproject"; // newproject.html 반환
    }

    /**
     * ✅ 프로젝트 생성 처리 (등록 버튼을 눌렀을 때 실행)
     */
    @PostMapping("/bw")
    public String createProject(@RequestParam("project-name") String projectName,
                                @RequestParam("project-description") String projectDescription,
                                @RequestParam("start-date") String startDateStr,
                                @RequestParam("end-date") String endDateStr,
                                @RequestParam("recruitment") int recruitment,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,  // 로그인한 사용자 정보 가져오기
                                Model model) {

        // 현재 로그인한 사용자의 닉네임 가져오기
        String creatorNick = userDetails.getUserNick();

        // String → LocalDate 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        // 새로운 프로젝트 생성
        ProjectEntity newProject = ProjectEntity.builder()
                .title(projectName)
                .description(projectDescription)
                .createdBy(creatorNick) // created_by 컬럼에 저장
                .creatorNick(creatorNick) // creator_nick 컬럼에도 동일한 값 저장
                .startDate(startDate)
                .endDate(endDate)
                .recruitmentPeriod(recruitment)
                .recruitmentCount(recruitment)
                .views(0)
                .likes(0)
                .status("모집중")
                .build();

        // DB에 저장
        ProjectEntity savedProject = projectService.saveProject(newProject);

        // 목록에도 반영되면서, 생성한 프로젝트 상세 페이지로 이동
        return "redirect:/postproject/" + savedProject.getId();
    }
}
