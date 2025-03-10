package com.example.bob.Controller;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.UserEntity;
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

    // 프로젝트 목록 페이지로 이동
    @GetMapping("/project")
    public String showProjects(Model model) {
        List<ProjectDTO> projectDTOs = projectService.getAllProjectsDTO();
        model.addAttribute("projects", projectDTOs);
        return "project";
    }

    // 내가 만든 프로젝트와 내가 참가한 프로젝트 페이지
    @GetMapping("/myproject")
    public String myProjectPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        UserEntity user = userDetails.getUserEntity();  // 로그인한 사용자 가져오기

        // 내가 만든 프로젝트 목록
        List<ProjectDTO> createdProjects = projectService.getCreatedProjects(user);  // UserEntity 전달
        model.addAttribute("createdProjects", createdProjects);

        // 내가 참가한 프로젝트 목록
        List<ProjectDTO> joinedProjects = projectService.getJoinedProjects(user);  // UserEntity 전달
        model.addAttribute("joinedProjects", joinedProjects);

        return "myproject";  // "myproject.html"로 리턴
    }

    // 프로젝트 상세 보기
    @GetMapping("/postproject/{id}")
    public String showProjectDetail(@PathVariable Long id,
                                    Model model,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProjectEntity project = projectService.getProjectById(id);
        projectService.incrementViews(id); // 조회수 증가

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        model.addAttribute("today", today);
        model.addAttribute("goal", project.getGoal());
        model.addAttribute("project", project);
        model.addAttribute("isOwner", project.getCreatedBy().equals(userDetails.getUserNick())); // 로그인한 사용자가 작성자인지 체크

        return "postproject";
    }

    // 프로젝트 삭제 API
    @DeleteMapping("/postproject/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteProject(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            projectService.deleteProject(id, userDetails.getUserNick());
            return ResponseEntity.ok("✅ 프로젝트가 삭제되었습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ 삭제 권한이 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 프로젝트 삭제 실패");
        }
    }

    // 좋아요 토글 API
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

    // 조회수 증가 API
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

    // 프로젝트 생성 페이지
    @GetMapping("/newproject")
    public String showNewProjectForm() {
        return "newproject";
    }

    // 프로젝트 생성 처리
    @PostMapping("/bw")
    public String createProject(@RequestParam("project-name") String projectName,
                                @RequestParam("project-description") String projectDescription,
                                @RequestParam("project-goal") String projectGoal,
                                @RequestParam("start-date") String startDateStr,
                                @RequestParam("end-date") String endDateStr,
                                @RequestParam("recruitment-start-date") String recruitmentStartStr,
                                @RequestParam("recruitment-end-date") String recruitmentEndStr,
                                @RequestParam("recruitment") String recruitmentStr,
                                @RequestParam(value = "recruitmentCount", required = false) String recruitmentCountStr,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                Model model) {

        String creatorNick = userDetails.getUserNick();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);
        LocalDate recruitmentStartDate = LocalDate.parse(recruitmentStartStr, formatter);
        LocalDate recruitmentEndDate = LocalDate.parse(recruitmentEndStr, formatter);

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
                .recruitmentStartDate(recruitmentStartDate)
                .recruitmentEndDate(recruitmentEndDate)
                .recruitmentPeriod(recruitment)
                .recruitmentCount(recruitment)
                .views(0)
                .likes(0)
                .status("모집중")
                .build();

        ProjectEntity savedProject = projectService.saveProject(newProject);
        return "redirect:/postproject/" + savedProject.getId();
    }

    // 프로젝트 수정 페이지로 이동
    @GetMapping("/postproject/{id}/edit")
    public String showEditProjectPage(@PathVariable Long id, Model model) {
        ProjectEntity project = projectService.getProjectById(id);
        model.addAttribute("project", project);
        return "editproject";
    }

    // 프로젝트 수정 처리
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
                                @RequestParam(value = "recruitmentCount", required = false, defaultValue = "0") Integer recruitmentCount,
                                Model model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        ProjectEntity existingProject = projectService.getProjectById(id);
        LocalDate recruitmentStartDate = (recruitmentStartStr != null && !recruitmentStartStr.isEmpty()) ?
                LocalDate.parse(recruitmentStartStr, formatter) : existingProject.getRecruitmentStartDate();
        LocalDate recruitmentEndDate = (recruitmentEndStr != null && !recruitmentEndStr.isEmpty()) ?
                LocalDate.parse(recruitmentEndStr, formatter) : existingProject.getRecruitmentEndDate();

        int recruitment = 0;
        try {
            if ("plus".equals(recruitmentStr)) {
                recruitment = recruitmentCount;
            } else {
                recruitment = Integer.parseInt(recruitmentStr);
            }
        } catch (NumberFormatException e) {
            model.addAttribute("error", "잘못된 모집 인원 값입니다.");
            return "editproject";
        }

        try {
            ProjectEntity updatedProject = projectService.updateProject(
                    id, projectName, projectDescription, projectGoal,
                    startDate, endDate, recruitmentStartDate, recruitmentEndDate,
                    recruitment, recruitmentCount
            );

            model.addAttribute("project", updatedProject);
            return "redirect:/postproject/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "프로젝트 수정에 실패했습니다.");
            return "editproject";
        }
    }

    // 프로젝트 생성 성공 후 성공 페이지로 이동
    @GetMapping("/success")
    public String showsuccessForm() {
        return "success";
    }

    // 프로젝트 신청 페이지 이동
    @GetMapping("/projectapplication")
    public String projectapplicationForm() {
        return "projectapplication";
    }

    // 팀 신청 페이지 이동
    @GetMapping("/teamrequest")
    public String teamrequestForm() {
        return "teamrequest";
    }

    // 프로젝트 신청 페이지 이동
    @GetMapping("/projapplication")
    public String projapplicationForm() {
        return "projapplication";
    }

    @GetMapping("/prohistory")
    public String prohistoryForm() {return "projecthistory";}
}
