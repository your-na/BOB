package com.example.bob.Controller;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        return "project"; // ✅ 목록 페이지 (project.html)
    }

    /**
     * ✅ 프로젝트 생성 페이지로 이동
     */
    @GetMapping("/bw")
    public String createProjectForm() {
        return "newproject"; // ✅ 프로젝트 생성 페이지
    }

    /**
     * ✅ 새로운 프로젝트 생성 (등록 후 상세 페이지로 이동)
     */
    @PostMapping("/bw")
    public String createProject(
            @RequestParam("project-name") String title,
            @RequestParam("project-description") String description,
            @RequestParam("recruitment") int recruitmentCount,
            @RequestParam("start-date") String startDate,
            @RequestParam("end-date") String endDate) {

        // ✅ 새로운 프로젝트 생성
        ProjectEntity project = ProjectEntity.builder()
                .title(title)
                .description(description)
                .recruitmentPeriod(calculateRecruitmentDays(startDate))
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .recruitmentCount(recruitmentCount)
                .createdBy("유나나콘") // ✅ 로그인 연동 후 수정 필요
                .views(0)
                .likes(0)
                .status("모집중")
                .build();

        // ✅ 프로젝트 저장 후 ID 확인
        ProjectEntity savedProject = projectService.saveProject(project);
        System.out.println("✅ 프로젝트 저장됨! ID: " + savedProject.getId());

        // ✅ 저장된 프로젝트의 상세 페이지로 리다이렉트
        return "redirect:/postproject/" + savedProject.getId();
    }

    /**
     * ✅ 프로젝트 상세 보기 (postproject.html)
     */
    @GetMapping("/postproject/{id}")
    public String showProjectDetail(@PathVariable Long id, Model model) {
        // ✅ 프로젝트 가져오기
        ProjectEntity project = projectService.getProjectById(id);

        // ✅ 로그 찍어서 ID 확인 (제대로 불러오는지 체크)
        System.out.println("✅ 프로젝트 상세 보기 요청! ID: " + id);
        System.out.println("✅ 프로젝트 정보: " + project);

        model.addAttribute("project", project);
        return "postproject"; // ✅ postproject.html 이동
    }

    /**
     * ✅ 모집 기간 계산 (예: 현재 날짜 기준 D-3)
     */
    private int calculateRecruitmentDays(String startDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate today = LocalDate.now();
        return (int) (start.toEpochDay() - today.toEpochDay());
    }
}
