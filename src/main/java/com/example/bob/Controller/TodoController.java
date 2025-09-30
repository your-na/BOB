package com.example.bob.Controller;

import com.example.bob.DTO.ContestTeamSimpleDTO;
import com.example.bob.DTO.ProjectDTO;
import com.example.bob.DTO.TodoRequestDto;
import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.TodoEntity;
import com.example.bob.Repository.ContestTeamRepository;
import com.example.bob.Repository.TodoRepository;
import com.example.bob.Service.ContestTeamService;
import com.example.bob.Service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.bob.security.UserDetailsImpl;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ProjectService;

import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin // 프론트와 연결 위해 필요
public class TodoController {

    private final TodoService todoService;
    private final Logger logger = LoggerFactory.getLogger(TodoController.class);
    private final ProjectService projectService;
    private final ContestTeamRepository contestTeamRepository;
    private final TodoRepository todoRepository;
    private final ContestTeamService contestTeamService;

    @PostMapping
    public TodoEntity createTodo(@RequestBody TodoRequestDto dto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();

        if ("개인".equals(dto.getWorkspace())) {
            return todoService.savePersonalTodo(dto, user);
        }

        if ("공모전".equals(dto.getType())) {
            // 공모전 팀 조회
            ContestTeamEntity team = contestTeamRepository.findById(dto.getTargetId())
                    .orElseThrow(() -> new RuntimeException("공모전 팀이 존재하지 않습니다."));

            boolean isHost = team.getCreatedBy().equals(user.getUserNick());
            return todoService.saveForContest(dto, user, isHost, team);
        }



        boolean isHost = projectService.isUserHost(dto.getWorkspace(), user.getUserNick());
        return todoService.save(dto, user, isHost);
    }

    @GetMapping
    public List<TodoEntity> getTodos(@RequestParam String date,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String userNick = userDetails.getUserEntity().getUserNick(); // 로그인한 유저의 닉네임
        return todoService.findByDateAndUserNick(date, userNick);   // 닉네임 기반으로 필터링
    }

    @GetMapping("/contest")
    public List<TodoEntity> getContestTodos(@RequestParam String date,
                                            @RequestParam Long teamId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoService.findByDateAndTeam(date, teamId, userDetails.getUserEntity());
    }


    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> updateTodoCompletion(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody) {

        boolean completed = requestBody.get("completed");
        System.out.println("📦 [PATCH] 체크 상태 변경 요청! ✅ id: " + id + ", completed: " + completed);

        todoService.updateCompletion(id, completed);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popup")
    @ResponseBody
    public List<TodoEntity> getPopupTodos(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoService.getTodosForPopup(userDetails.getUserEntity().getUserNick());
    }

    @GetMapping("/contest/team/{teamId}/members")
    public ResponseEntity<List<String>> getTeamMembers(@PathVariable Long teamId) {
        ContestTeamEntity team = contestTeamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));

        List<String> members = contestTeamService.getAcceptedMemberNicks(team);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/my-contest-teams")
    public ResponseEntity<List<ContestTeamSimpleDTO>> getMyContestTeams(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();
        List<ContestTeamEntity> teams = new ArrayList<>();
        teams.addAll(contestTeamService.getContestsLedByUser(user));
        teams.addAll(contestTeamService.getContestsJoinedByUser(user));

        // ✅ 진행중인 팀만 필터링
        List<ContestTeamSimpleDTO> dtoList = teams.stream()
                .map(ContestTeamSimpleDTO::from)
                .toList();

        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/members")
    public ResponseEntity<?> getWorkspaceMembers(@RequestParam String workspace,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String currentUserNick = userDetails.getUserEntity().getUserNick();

        // 공모전 팀
        Optional<ContestTeamEntity> contestTeamOpt = contestTeamRepository.findByTeamName(workspace);
        if (contestTeamOpt.isPresent()) {
            ContestTeamEntity team = contestTeamOpt.get();
            List<String> members = contestTeamService.getAcceptedMemberNicks(team);

            // ✅ 팀장을 명시적으로 추가 (중복 없을 때만)
            if (!members.contains(team.getCreatedBy())) {
                members.add(team.getCreatedBy());
            }

            return ResponseEntity.ok(Map.of(
                    "creator", team.getCreatedBy(),
                    "currentUser", currentUserNick,
                    "members", members
            ));
        }

        // 프로젝트 팀
        Optional<ProjectEntity> projectOpt = projectService.findByTitle(workspace);
        if (projectOpt.isPresent()) {
            List<String> members = projectService.getProjectMemberNicknames(workspace);
            return ResponseEntity.ok(Map.of(
                    "creator", projectOpt.get().getCreatedBy(),
                    "currentUser", currentUserNick,
                    "members", members
            ));
        }

        return ResponseEntity.status(404).body("팀 정보를 찾을 수 없습니다.");
    }

    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectDTO>> getMyProjects(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();
        List<ProjectDTO> created = projectService.getCreatedProjects(user);
        List<ProjectDTO> joined = projectService.getJoinedProjects(user);

        created.addAll(joined);

        // ✅ 진행중인 프로젝트만 반환
        List<ProjectDTO> filtered = created.stream()
                .filter(proj -> "진행중".equals(proj.getStatus()))
                .toList();

        return ResponseEntity.ok(filtered);
    }
}
