package com.example.bob.Controller;

import com.example.bob.Entity.*;
import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Service.ContestTeamService;
import com.example.bob.Service.ProjectService;
import com.example.bob.security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // ✅ 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // ✅ 추가

@Controller
public class SpaceController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ContestTeamService contestTeamService;

    // 프로젝트 목록
    @GetMapping("/plan")
    public String getUserProjects(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity userEntity = userDetails.getUserEntity();

        // ✅ 기존 프로젝트
        List<ProjectDTO> createdProjects = projectService.getCreatedProjects(userEntity);
        List<ProjectDTO> joinedProjects = projectService.getJoinedProjects(userEntity);

        // ✅ 공모전 팀 - 팀장 목록
        List<ContestTeamEntity> leaderContests = contestTeamService.getContestsLedByUser(userEntity);

        // ✅ 공모전 팀 - 참여자 목록
        List<ContestTeamEntity> joinedContests = contestTeamService.getContestsJoinedByUser(userEntity);


        model.addAttribute("createdProjects", createdProjects);
        model.addAttribute("joinedProjects", joinedProjects);
        model.addAttribute("leaderContests", leaderContests);   // 공모전 팀장 목록
        model.addAttribute("joinedContests", joinedContests);   // 공모전 팀원 목록

        return "plan";
    }

    @GetMapping("/todohome/{id}")
    public String showProjectDetail(@PathVariable("id") Long projectId, Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity userEntity = userDetails.getUserEntity();
        ProjectEntity project = projectService.getProjectById(projectId);

        // 팀원 목록을 가져옴
        List<UserProjectEntity> userProjects = project.getUserProjects();
        List<String> teamMembers = new ArrayList<>();
        UserEntity owner = null;

        // 프로젝트의 주최자 정보는 createdBy 필드에서 가져옴
        String createdBy = project.getCreatedBy();  // 프로젝트의 주최자 닉네임
        System.out.println("프로젝트 주최자: " + createdBy);  // 디버그: 주최자 닉네임 확인

        for (UserProjectEntity userProject : userProjects) {
            String status = userProject.getStatus(); // 상태 확인
            System.out.println("참여자: " + userProject.getUser().getUserNick() + " / 상태: " + status);

            // ✅ 모집중 또는 진행중인 사람만 추가
            if ("모집중".equals(status) || "진행중".equals(status)) {
                // ✅ 주최자가 아니면 teamMembers에 추가
                if (!userProject.getUser().getUserNick().equals(createdBy)) {
                    teamMembers.add(userProject.getUser().getUserNick());
                } else {
                    owner = userProject.getUser(); // 주최자는 따로 저장
                }
            }
        }

        // 주최자 정보가 null이면 createdBy를 기반으로 owner 설정
        if (owner == null && createdBy != null) {
            owner = projectService.getUserByNick(createdBy); // createdBy로 사용자 정보를 찾는 방법
        }

        // 모델에 프로젝트 및 팀원 정보 추가
        model.addAttribute("project", project);
        model.addAttribute("goal", project.getGoal());
        model.addAttribute("teamMembers", teamMembers); // 팀원들
        model.addAttribute("owner", owner); // 주최자 정보

        // 로그로 owner 확인
        if (owner != null) {
            System.out.println("주최자: " + owner.getUserNick());
        } else {
            System.out.println("주최자 정보가 없습니다.");
        }

        return "todo_home"; // todo_home.html로 프로젝트 정보를 넘김
    }



    // ✅ 공지사항 조회 (팀원, 팀장 모두 가능)
    @ResponseBody
    @GetMapping("/api/project/{id}/notice")
    public ResponseEntity<String> getNotice(@PathVariable Long id) {
        String notice = projectService.getNotice(id);
        return ResponseEntity.ok(notice);
    }

    // ✅ 공지사항 수정 (팀장만 가능)
    @ResponseBody
    @PatchMapping("/api/project/{id}/notice")
    public ResponseEntity<?> updateNotice(@PathVariable Long id,
                                          @RequestBody Map<String, String> request,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String content = request.get("content");
        projectService.updateNotice(id, content, userDetails.getUserEntity());
        return ResponseEntity.ok().build();
    }

    // ✅ 할 일 팝업
    @GetMapping("/popup")
    public String openTaskPopup(Model model) {
        return "task_popup";  // task_popup.html (thymeleaf templates 기준)
    }


    // /todo_plan/{id}로 요청을 처리할 수 있게 설정
    @GetMapping("/todo_plan/{id}")
    public String todoPlanPage(@PathVariable Long id, Model model) {
        // 프로젝트 ID로 프로젝트를 조회
        ProjectEntity project = projectService.getProjectById(id);
        // 실제 프로젝트 제목을 모델에 추가
        model.addAttribute("projectTitle", project.getTitle());
        return "todo_plan"; // "todo_plan.html" 템플릿 반환
    }



}
