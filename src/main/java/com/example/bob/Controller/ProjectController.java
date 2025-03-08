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
    public String updateProject(@PathVariable Long id,
                                @RequestParam("project-name") String projectName,
                                @RequestParam("project-description") String projectDescription,
                                @RequestParam("project-goal") String projectGoal,
                                @RequestParam("start-date") String startDateStr,
                                @RequestParam("end-date") String endDateStr,
                                @RequestParam("recruitment") String recruitmentStr,
                                @RequestParam(value = "recruitmentCount", required = false) String recruitmentCountStr,
                                Model model) {

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
                return "editproject";
            }
        } else {
            try {
                recruitment = Integer.parseInt(recruitmentStr);
            } catch (NumberFormatException e) {
                model.addAttribute("error", "잘못된 모집 인원 값입니다.");
                return "editproject";
            }
        }

        try {
            ProjectEntity updatedProject = projectService.updateProject(
                    id,
                    projectName,
                    projectDescription,
                    projectGoal,
                    startDate,
                    endDate,
                    recruitment
            );
            model.addAttribute("project", updatedProject);
            return "redirect:/postproject/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "프로젝트 수정에 실패했습니다.");
            return "editproject";
        }
    }

    /**
     * ✅ 성공 페이지 이동
     */
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
