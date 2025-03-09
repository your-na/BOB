package com.example.bob.Controller;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Service.ProjectService;
import com.example.bob.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@Transactional
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * ✅ 프로젝트 목록 페이지로 이동
     */
    @GetMapping("/project")
    public String showProjects(Model model) {
        List<ProjectDTO> projectDTOs = projectService.getAllProjectsDTO();
        model.addAttribute("projects", projectDTOs);
        return "project";
    }

    /**
     * ✅ 프로젝트 상세 보기 (postproject.html)
     */
    @GetMapping("/postproject/{id}")
    public String showProjectDetail(@PathVariable Long id, Model model) {
        ProjectEntity project = projectService.getProjectById(id);
        project = projectService.incrementViews(id);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        model.addAttribute("today", today);
        model.addAttribute("goal", project.getGoal());
        model.addAttribute("project", project);
        return "postproject";
    }

    /**
     * ✅ 프론트엔드(Vue, React)에서 사용 가능한 REST API 추가 (JSON 반환)
     */
    @GetMapping("/api/projects/{id}")
    @ResponseBody
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long id) {
        ProjectEntity project = projectService.getProjectById(id);
        return ResponseEntity.ok(project); // JSON 데이터 반환
    }

    /**
     * ✅ 프로젝트 삭제 API (CSRF 토큰 포함)
     */
    @DeleteMapping("/postproject/{id}")  // ✅ `/delete` 제거
    @ResponseBody
    public ResponseEntity<String> deleteProject(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            System.out.println("🚀 DELETE 요청 도착! 프로젝트 ID: " + id); // ✅ 요청 로그 추가
            projectService.deleteProject(id, userDetails.getUserNick());
            return ResponseEntity.ok("✅ 프로젝트가 삭제되었습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ 삭제 권한이 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 프로젝트 삭제 실패");
        }
    }

    /**
     * ✅ 좋아요 토글 API
     */
    @PostMapping("/postproject/{id}/like")
    @ResponseBody
    public ResponseEntity<?> likeProject(@PathVariable Long id, @RequestParam Long userId) {
        try {
            ProjectEntity updatedProject = projectService.toggleLike(id, userId);
            return ResponseEntity.ok(updatedProject.getLikes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("좋아요 요청 실패");
        }
    }

    /**
     * ✅ 조회수 증가 API
     */
    @PostMapping("/postproject/{id}/incrementViews")
    @ResponseBody
    public ResponseEntity<?> incrementViews(@PathVariable Long id) {
        try {
            projectService.incrementViews(id);
            return ResponseEntity.ok("조회수 증가 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("조회수 증가 실패");
        }
    }

    /**
     * ✅ 프로젝트 생성 페이지
     */
    @GetMapping("/newproject")
    public String showNewProjectForm() {
        return "newproject";
    }

    /**
     * ✅ 프로젝트 생성 처리
     */
    @PostMapping("/bw")
    public String createProject(@RequestParam("project-name") String projectName,
                                @RequestParam("project-description") String projectDescription,
                                @RequestParam("project-goal") String projectGoal,
                                @RequestParam("start-date") String startDateStr,
                                @RequestParam("end-date") String endDateStr,
                                @RequestParam("recruitment") String recruitmentStr,
                                @RequestParam(value = "recruitmentCount", required = false) String recruitmentCountStr,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                Model model) {

        String creatorNick = userDetails.getUserNick();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        int recruitment = 0;
        if ("기타".equals(recruitmentStr)) {
            try {
                if (recruitmentCountStr != null && !recruitmentCountStr.isEmpty()) {
                    recruitment = Integer.parseInt(recruitmentCountStr);
                }
            } catch (NumberFormatException e) {
                model.addAttribute("error", "잘못된 모집 인원 값입니다.");
                return "newproject";
            }
        } else {
            try {
                recruitment = Integer.parseInt(recruitmentStr);
            } catch (NumberFormatException e) {
                model.addAttribute("error", "잘못된 모집 인원 값입니다.");
                return "newproject";
            }
        }

        ProjectEntity newProject = ProjectEntity.builder()
                .title(projectName)
                .description(projectDescription)
                .goal(projectGoal)
                .createdBy(creatorNick)
                .creatorNick(creatorNick)
                .startDate(startDate)
                .endDate(endDate)
                .recruitmentPeriod(recruitment)
                .recruitmentCount(recruitment)
                .views(0)
                .likes(0)
                .status("모집중")
                .build();

        ProjectEntity savedProject = projectService.saveProject(newProject);
        return "redirect:/postproject/" + savedProject.getId();
    }

    /**
     * ✅ 프로젝트 수정 페이지로 이동
     */
    @GetMapping("/postproject/{id}/edit")
    public String showEditProjectPage(@PathVariable Long id, Model model) {
        ProjectEntity project = projectService.getProjectById(id);
        model.addAttribute("project", project);
        return "editproject";
    }

    /**
     * ✅ 프로젝트 수정 처리
     */
    @PostMapping("/postproject/{id}/edit")
    @Transactional
    public String updateProject(@PathVariable Long id,
                                @RequestParam("project-name") String projectName,
                                @RequestParam("project-description") String projectDescription,
                                @RequestParam("project-goal") String projectGoal,
                                @RequestParam("start-date") String startDateStr,
                                @RequestParam("end-date") String endDateStr,
                                @RequestParam(value = "recruitment-start-date", required = false) String recruitmentStartStr,
                                @RequestParam(value = "recruitment-end-date", required = false) String recruitmentEndStr,
                                @RequestParam(value = "recruitment", required = false) String recruitmentStr,
                                @RequestParam(value = "recruitmentCount", required = false, defaultValue = "0") Integer recruitmentCount, // ✅ 기본값 설정
                                Model model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        // ✅ 기존 프로젝트 데이터 가져오기
        ProjectEntity existingProject = projectService.getProjectById(id);
        LocalDate recruitmentStartDate = (recruitmentStartStr != null && !recruitmentStartStr.isEmpty()) ?
                LocalDate.parse(recruitmentStartStr, formatter) : existingProject.getRecruitmentStartDate();
        LocalDate recruitmentEndDate = (recruitmentEndStr != null && !recruitmentEndStr.isEmpty()) ?
                LocalDate.parse(recruitmentEndStr, formatter) : existingProject.getRecruitmentEndDate();

        System.out.println("🚀 모집 시작일 요청 값: " + recruitmentStartStr);
        System.out.println("🚀 모집 종료일 요청 값: " + recruitmentEndStr);
        System.out.println("🚀 모집 인원 요청 값: " + recruitmentCount);

        // ✅ `recruitmentStr`을 정수 값으로 변환
        int recruitment = 0;
        try {
            if ("plus".equals(recruitmentStr)) {
                recruitment = recruitmentCount; // 직접 입력 값 사용
            } else {
                recruitment = Integer.parseInt(recruitmentStr); // 기존 select 값 사용
            }
        } catch (NumberFormatException e) {
            model.addAttribute("error", "잘못된 모집 인원 값입니다.");
            return "editproject";
        }

        try {
            ProjectEntity updatedProject = projectService.updateProject(
                    id, projectName, projectDescription, projectGoal,
                    startDate, endDate, recruitmentStartDate, recruitmentEndDate,
                    recruitment, recruitmentCount // ✅ 이제 `recruitmentCount`가 올바르게 전달됨
            );

            model.addAttribute("project", updatedProject);
            return "redirect:/postproject/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "프로젝트 수정에 실패했습니다.");
            return "editproject";
        }
    }

    @DeleteMapping("/postproject/{id}/delete")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("✅ 프로젝트가 삭제되었습니다.");
    }


    @GetMapping("/success")
    public String showsuccessForm() {
        return "success";
    }

    @GetMapping("/projectapplication")
    public String projectapplicationForm() {
        return "projectapplication";
    }

    @GetMapping("/teamrequest")
    public String teamrequestForm() {
        return "teamrequest";
    }

    @GetMapping("/projapplication")
    public String projapplicationForm() { return "projapplication"; }

    @GetMapping("/myproject")
    public String myprojectForm() { return "myproject"; }

}
