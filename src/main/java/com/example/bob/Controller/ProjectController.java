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
     * 프로젝트 목록 페이지로 이동
     * @param model 프로젝트 데이터를 전달하기 위한 모델
     * @return project.html 렌더링
     */
    @GetMapping("/project")
    public String showProjects(Model model) {
        List<ProjectEntity> projects = projectService.getAllProjects(); // DB에서 모든 프로젝트 가져오기
        model.addAttribute("projects", projects); // 모델에 데이터 추가
        return "project"; // project.html
    }

    /**
     * 프로젝트 생성 페이지로 이동
     * @return newproject.html 렌더링
     */
    @GetMapping("/bw")
    public String createProjectForm() {
        return "newproject"; // 프로젝트 생성 페이지
    }

    /**
     * 새로운 프로젝트 생성
     * @param title 프로젝트 제목
     * @param description 프로젝트 설명
     * @param recruitmentCount 모집 인원
     * @param startDate 프로젝트 시작일
     * @param endDate 프로젝트 종료일
     * @return 프로젝트 목록 페이지로 리다이렉트
     */
    @PostMapping("/bw")
    public String createProject(
            @RequestParam("project-name") String title,
            @RequestParam("project-description") String description,
            @RequestParam("recruitment") int recruitmentCount,
            @RequestParam("start-date") String startDate,
            @RequestParam("end-date") String endDate) {

        // 새로운 프로젝트 엔티티 생성
        ProjectEntity project = ProjectEntity.builder()
                .title(title)
                .description(description)
                .recruitmentPeriod(calculateRecruitmentDays(startDate))
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .recruitmentCount(recruitmentCount)
                .createdBy("유나나콘") // 임시로 생성자 이름 설정 (로그인 연동 시 수정 필요)
                .views(0) // 조회수 초기화
                .likes(0) // 좋아요 초기화
                .status("모집중") // 초기 상태 설정
                .build();

        // 프로젝트 저장
        projectService.saveProject(project);

        // 프로젝트 목록 페이지로 리다이렉트
        return "redirect:/project";
    }

    /**
     * 모집 기간 계산 (예: 현재 날짜 기준 D-3)
     * @param startDate 시작 날짜
     * @return 모집 기간 (예: 3)
     */
    private int calculateRecruitmentDays(String startDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate today = LocalDate.now();
        return (int) (start.toEpochDay() - today.toEpochDay());
    }
}
