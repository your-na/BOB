package com.example.bob.Controller;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Repository.UserProjectRepository;
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
import com.example.bob.Repository.UserRepository;
import com.example.bob.Repository.ProjectRepository;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;




import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@Transactional
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    @GetMapping("/project")
    public String projectList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size,
                              Model model) {
        // ✅ 페이징된 프로젝트 리스트 가져오기
        Page<ProjectDTO> projectPage = projectService.getPagedProjects(PageRequest.of(page, size));

        // ✅ 모델에 데이터 전달
        model.addAttribute("projects", projectPage.getContent());  // 현재 페이지의 프로젝트들
        model.addAttribute("currentPage", page);                   // 현재 페이지 번호
        model.addAttribute("totalPages", projectPage.getTotalPages()); // 전체 페이지 수

        return "project";
    }

    // 메인 페이지에서 JSON 응답을 받는 엔드포인트
    @GetMapping("/project/api")
    public ResponseEntity<List<ProjectDTO>> getProjects() {
        List<ProjectDTO> activeProjects = projectService.getAllProjectsDTO();  // 완료된 프로젝트 제외
        return ResponseEntity.ok(activeProjects);  // JSON 형식으로 반환
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

    // ✅ ProjectController.java 안에 추가해줘!
    @GetMapping("/api/my-projects")
    @ResponseBody
    public List<ProjectDTO> getMyProjects(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();

        // 내가 만든 프로젝트 목록
        List<ProjectDTO> createdProjects = projectService.getCreatedProjects(user);

        // 내가 참가한 프로젝트 목록 (단, 내가 만든 건 제외됨)
        List<ProjectDTO> joinedProjects = projectService.getJoinedProjects(user);

            // ✅ 중복 제거 + 순서 유지
            Set<ProjectDTO> allProjects = new LinkedHashSet<>();
            allProjects.addAll(createdProjects);
            allProjects.addAll(joinedProjects);

            return new ArrayList<>(allProjects);
    }

    @GetMapping("/api/project-members")
    @ResponseBody
    public Map<String, Object> getProjectMembers(
            @RequestParam String title,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return projectService.getProjectMembersInfo(title, userDetails.getUserEntity());
    }

    // 프로젝트 상세 보기
    @GetMapping("/postproject/{id}")
    public String showProjectDetail(@PathVariable Long id,
                                    Model model,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProjectEntity project = projectService.getProjectById(id);
        projectService.incrementViews(id); // 조회수 증가

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        // ✅ 로그인한 사용자 정보
        UserEntity user = userDetails.getUserEntity();

        // ✅ 좋아요 눌렀는지 여부 확인
        boolean isLiked = project.getLikedUsers().contains(user.getUserId());

        // ✅ 좋아요 수 가져오기
        int likesCount = project.getLikes();

        // ✅ 현재 참여자 수 계산 (승인된 인원만 카운트할 수도 있음)
        int currentParticipants = (int) project.getUserProjects().stream()
                .filter(up -> !"신청중".equals(up.getStatus())) // 신청중 제외
                .count();
        project.setCurrentParticipants(currentParticipants);

        // ✅ 모델에 담기
        model.addAttribute("today", today);
        model.addAttribute("goal", project.getGoal());
        model.addAttribute("project", project);
        model.addAttribute("isOwner", project.getCreatedBy().equals(userDetails.getUserNick()));
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likesCount", likesCount);

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

    // 좋아요
    @PostMapping("/project/{id}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();

        ProjectEntity project = projectService.toggleLike(id, user.getUserId()); // 좋아요 처리

        boolean liked = project.getLikedUsers().contains(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked); // 현재 좋아요 상태
        response.put("likes", project.getLikes()); // 좋아요 수

        return response;
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
    public String createProject(
            @RequestParam("project-name") String projectName,
            @RequestParam("project-description") String projectDescription,
            @RequestParam("project-goal") String projectGoal,
            @RequestParam("start-date") String startDateStr,
            @RequestParam("end-date") String endDateStr,
            @RequestParam("recruitment-start-date") String recruitmentStartStr,
            @RequestParam("recruitment-end-date") String recruitmentEndStr,
            @RequestParam("recruitment") String recruitmentStr,
            @RequestParam(value = "recruitmentCount", required = false) String recruitmentCountStr,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 확인 로그
        System.out.println("Received recruitmentCount: " + recruitmentCountStr);

        String creatorNick = userDetails.getUserNick();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);
        LocalDate recruitmentStartDate = LocalDate.parse(recruitmentStartStr, formatter);
        LocalDate recruitmentEndDate = LocalDate.parse(recruitmentEndStr, formatter);

        int recruitmentCount = 0;

        if ("plus".equals(recruitmentStr)) {
            if (recruitmentCountStr != null && !recruitmentCountStr.isEmpty()) {
                try {
                    recruitmentCount = Integer.parseInt(recruitmentCountStr);
                } catch (NumberFormatException e) {
                    return "redirect:/bw";  // 잘못된 값일 경우 리다이렉트
                }
            } else {
                return "redirect:/bw";  // 모집 인원 미입력 시 리다이렉트
            }
        } else {
            try {
                recruitmentCount = Integer.parseInt(recruitmentStr);
            } catch (NumberFormatException e) {
                return "redirect:/bw";  // 잘못된 값일 경우 리다이렉트
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
                .recruitmentCount(recruitmentCount)
                .views(0)
                .likes(0)
                .status("모집중")
                .build();

        // 프로젝트 저장
        ProjectEntity savedProject = projectService.saveProject(newProject, recruitmentCountStr);

        // 프로젝트 상세 페이지로 리다이렉트
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


    // 프로젝트 참가 신청서 페이지로 이동
    @GetMapping("/projectapplication")
    public String showProjectApplicationPage(@RequestParam Long projectId,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails,
                                             Model model) {
        // 로그인한 사용자 정보 가져오기
        UserEntity userEntity = userDetails.getUserEntity();

        // 프로젝트 정보 가져오기
        ProjectEntity project = projectService.getProjectById(projectId);

        model.addAttribute("user", userEntity);
        model.addAttribute("project", project);

        return "projectapplication";  // 참가 신청서 페이지를 반환
    }



    // 프로젝트 신청 처리
    @PostMapping("/projectapplication")
    public String submitApplication(@RequestParam Long projectId,
                                    @RequestParam String message,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails,
                                    Model model) {
        // 로그인한 사용자 정보 가져오기
        UserEntity userEntity = userDetails.getUserEntity();

        // 프로젝트 정보 가져오기
        ProjectEntity project = projectService.getProjectById(projectId);

        // 신청 내용 저장 (예: 데이터베이스에 저장)
        projectService.submitApplication(userEntity, project, message);

        // 신청 후 알림 생성
        projectService.sendTeamRequestNotification(projectId, userEntity.getUserNick());  // 수정된 부분

        // 신청 완료 후 success 페이지로 리디렉션
        return "redirect:/success?projectId=" + project.getId();  // 프로젝트 ID를 쿼리 파라미터로 전달
    }

    @GetMapping("/projapplication2")
    public String showSubmittedApplication(@RequestParam Long projectId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails,
                                           Model model) {

        if (userDetails == null) {
            throw new IllegalStateException("❌ 로그인된 유저 정보가 없습니다.");
        }

        UserEntity user = userDetails.getUserEntity();
        if (user == null) {
            throw new IllegalStateException("❌ UserEntity가 없습니다.");
        }

        ProjectEntity project = projectService.getProjectById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("❌ 해당 ID에 프로젝트가 없습니다: " + projectId);
        }

        String submittedMessage = projectService.getSubmittedMessage(projectId, user);

        // 디버깅 로그
        System.out.println("✅ userName: " + user.getUserName());
        System.out.println("✅ projectTitle: " + project.getTitle());
        System.out.println("✅ submittedMessage: " + submittedMessage);

        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("message", submittedMessage);

        return "projapplication2";
    }




    // 성공 페이지 처리
    @GetMapping("/success")
    public String showSuccessPage(@RequestParam Long projectId, Model model) {
        // 프로젝트 정보 가져오기
        ProjectEntity project = projectService.getProjectById(projectId);

        // 현재 날짜 (제출 날짜)
        LocalDate submissionDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = submissionDate.format(formatter);  // 실시간 날짜

        // 모집 종료일이 null일 경우 기본 값 처리
        String formattedEndDate = project.getRecruitmentEndDate() != null ?
                project.getRecruitmentEndDate().format(formatter) : "미정";  // null이면 '미정' 표시

        // 모델에 프로젝트 정보와 포맷된 날짜 추가
        model.addAttribute("project", project);
        model.addAttribute("formattedStartDate", formattedStartDate);
        model.addAttribute("formattedEndDate", formattedEndDate);

        // 성공 페이지를 반환
        return "success";
    }



    // ✅ 신청 수락 API
    @PostMapping("/teamrequest/accept")
    @ResponseBody
    public ResponseEntity<String> acceptTeamRequest(@RequestBody Map<String, Object> requestData,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // ✅ 요청 데이터 디버깅
            System.out.println("✅ [DEBUG] 요청 데이터: " + requestData);

            // ✅ 값이 null이 아니고 숫자로 변환 가능한지 확인
            if (!requestData.containsKey("projectId") || !requestData.containsKey("userId")) {
                throw new IllegalArgumentException("❌ projectId 또는 userId가 요청에 없습니다.");
            }

            Long projectId = Long.valueOf(requestData.get("projectId").toString());
            Long userId = Long.valueOf(requestData.get("userId").toString());

            System.out.println("✅ [DEBUG] 수락 요청 - projectId: " + projectId + ", userId: " + userId);

            projectService.acceptTeamRequest(projectId, userId, userDetails.getUserEntity());
            return ResponseEntity.ok("✅ 신청이 수락되었습니다!");
        } catch (NullPointerException | NumberFormatException e) {
            return ResponseEntity.badRequest().body("❌ 요청 데이터가 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 신청 수락 중 오류 발생");
        }
    }

    // ❌ 신청 거절 API
    @PostMapping("/teamrequest/reject")
    @ResponseBody
    public ResponseEntity<String> rejectTeamRequest(@RequestBody Map<String, Object> requestData,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // ✅ 요청 데이터 디버깅
            System.out.println("✅ [DEBUG] 요청 데이터: " + requestData);

            // ✅ 값이 null이 아니고 숫자로 변환 가능한지 확인
            if (!requestData.containsKey("projectId") || !requestData.containsKey("userId")) {
                throw new IllegalArgumentException("❌ projectId 또는 userId가 요청에 없습니다.");
            }

            Long projectId = Long.valueOf(requestData.get("projectId").toString());
            Long userId = Long.valueOf(requestData.get("userId").toString());

            System.out.println("✅ [DEBUG] 거절 요청 - projectId: " + projectId + ", userId: " + userId);

            // ✅ 신청 거절 로직 실행 (수락 로직을 베끼는 대신 거절 메서드 호출!)
            projectService.rejectTeamRequest(projectId, userId, userDetails.getUserEntity());

            return ResponseEntity.ok("🚫 신청이 거절되었습니다!");
        } catch (NullPointerException | NumberFormatException e) {
            return ResponseEntity.badRequest().body("❌ 요청 데이터가 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 신청 거절 중 오류 발생");
        }
    }


    // ✅ 신청서 페이지 렌더링 (이걸 추가해야 함!)
    @GetMapping("/teamrequest/{projectId}/{senderId}")
    public String showTeamRequestPage(@PathVariable Long projectId,
                                      @PathVariable Long senderId,
                                      Model model) {
        // ✅ 신청한 유저 정보 가져오기
        UserEntity user = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 사용자를 찾을 수 없습니다. (ID: " + senderId + ")"));

        // ✅ 프로젝트 정보 가져오기
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 프로젝트를 찾을 수 없습니다. (ID: " + projectId + ")"));

        // ✅ 디버깅 로그 추가
        System.out.println("✅ [DEBUG] 프로젝트 ID: " + projectId);
        System.out.println("✅ [DEBUG] 신청자 ID: " + senderId);
        System.out.println("✅ [DEBUG] 신청자 닉네임: " + user.getUserNick());
        System.out.println("✅ [DEBUG] 프로젝트 제목: " + project.getTitle());

        // ✅ 모델에 데이터 추가 (Thymeleaf에서 사용할 수 있도록!)
        model.addAttribute("userNick", user.getUserNick());  // 신청자 닉네임
        model.addAttribute("projectTitle", project.getTitle());  // 프로젝트 제목
        model.addAttribute("projectId", projectId); // 프로젝트 ID
        model.addAttribute("userId", senderId); // ✅ 신청한 유저 ID (수정: senderId → userId)

        return "teamrequest"; // ✅ teamrequest.html 페이지 렌더링
    }

    // ✅ 쿼리 파라미터 방식 URL도 지원
    @GetMapping("/teamrequest")
    public String showTeamRequestPageByQueryParam(@RequestParam Long projectId,
                                                  @RequestParam Long userId,
                                                  Model model) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 사용자를 찾을 수 없습니다."));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 프로젝트를 찾을 수 없습니다."));

        model.addAttribute("userNick", user.getUserNick());
        model.addAttribute("projectTitle", project.getTitle());
        model.addAttribute("projectId", projectId);
        model.addAttribute("userId", userId);

        return "teamrequest";
    }




    @GetMapping("/history")
    public String showHistoryPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Long userId = userDetails.getUserEntity().getUserId(); // 로그인한 유저 ID

        // ✅ 제출된 파일이 있고, 완료 상태이고, visible=true인 내역만 가져오기
        List<UserProjectEntity> completedUserProjects =
                userProjectRepository.findByUser_UserIdAndStatusAndSubmittedFileNameIsNotNullAndVisibleTrue(userId, "완료");

        model.addAttribute("completedProjects", completedUserProjects); // ✅ history.html에서 사용됨

        // ✅ 공모전 등 추가하려면 여기에 추가
        // model.addAttribute("submittedContests", ...);

        return "history";
    }


    // ✅ 프로젝트 기록 삭제 API
    @DeleteMapping("/project-history/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteProjectHistory(@PathVariable Long id) {
        try {
            userProjectRepository.deleteById(id);
            return ResponseEntity.ok("삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/projecthistory")
    public String showProjectHistoryPage() {
        return "projecthistory"; // templates/projecthistory.html 파일을 반환
    }


    @GetMapping("/todoadd")
    public String showAddPage() {
        return "todo_add";
    }


}

