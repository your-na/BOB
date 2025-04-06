package com.example.bob.Service;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.ProjectHistoryEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Repository.ProjectHistoryRepository;
import com.example.bob.Repository.ProjectRepository;
import com.example.bob.Repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Entity.NotificationEntity;
import com.example.bob.Repository.NotificationRepository;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;


@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository projectHistoryRepository;
    private final UserProjectRepository userProjectRepository; // UserProjectRepository 추가
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    // 멤버 변수로 projectFilePath를 선언
    private final String projectFilePath = "C:/uploads/project/";
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    @PersistenceContext
    private EntityManager entityManager;  // EntityManager 주입

    /**
     * ✅ 모든 프로젝트를 DTO로 변환하여 반환
     */
    public List<ProjectDTO> getAllProjectsDTO() {
        // 최신 프로젝트가 먼저 나올 수 있도록 id 기준으로 내림차순 정렬
        return projectRepository.findAllActiveProjectsSortedById().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * ✅ 특정 프로젝트 가져오기
     */
    @Transactional(readOnly = true)
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ 해당 프로젝트가 없습니다. ID=" + id));
    }

    /**
     * ✅ 프로젝트 저장 후 반환
     */
    @Transactional
    public ProjectEntity saveProject(ProjectEntity project, String customRecruitmentCount) {
        logger.info("🚀 프로젝트 저장 전 모집 종료일: {}", project.getRecruitmentEndDate());

        // 기본 상태를 "모집중"으로 설정
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("모집중");
        }

        // userProjects가 null이면 빈 리스트로 초기화
        if (project.getUserProjects() == null) {
            project.setUserProjects(new ArrayList<>());
        }

        // ✅ 주최자도 참여자 목록에 추가
        UserEntity creator = userRepository.findByUserNick(project.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("❌ 주최자 정보를 찾을 수 없습니다."));

        UserProjectEntity hostUserProject = UserProjectEntity.builder()
                .user(creator)
                .project(project)
                .status("진행중") // 기본 상태, 필요시 "완료"도 가능
                .visible(true)    // 삭제된 상태 아님
                .build();

        project.getUserProjects().add(hostUserProject);

        // 주최자의 상태에 따라 프로젝트 상태를 설정
        String ownerStatus = hostUserProject.getStatus();
        if ("진행중".equals(ownerStatus)) {
            project.setStatus("진행중");
        } else if ("완료".equals(ownerStatus)) {
            project.setStatus("완료");
        }

        project.updateStatus();  // 상태 최종 업데이트

        // customRecruitmentCount 값이 있으면 모집 인원 수를 설정
        if (customRecruitmentCount != null && !customRecruitmentCount.isEmpty()) {
            try {
                int recruitmentCount = Integer.parseInt(customRecruitmentCount);  // 수동 입력 값 반영
                project.setRecruitmentCount(recruitmentCount);  // 모집 인원 설정
            } catch (NumberFormatException e) {
                logger.error("❌ 모집 인원 입력 값이 유효하지 않습니다.");
                throw new IllegalArgumentException("모집 인원 입력 값이 유효하지 않습니다.");
            }
        } else {
            if (project.getRecruitmentCount() <= 0) {
                throw new IllegalArgumentException("모집 인원 수는 1명 이상이어야 합니다.");
            }
        }

        // 프로젝트 저장
        ProjectEntity savedProject = projectRepository.save(project);
        logger.info("✅ 저장된 프로젝트의 상태: {}", savedProject.getStatus());

        // 프로젝트 히스토리 저장
        saveProjectHistory(savedProject, "생성됨");

        return savedProject;
    }



    /**
     * ✅ 프로젝트 삭제 (히스토리 저장 후 실제로 삭제)
     */
    @Transactional
    public void deleteProject(Long id, String userNick) {
        // 프로젝트 찾기
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 프로젝트가 없습니다."));

        String owner = project.getCreatedBy();
        if (!owner.equals(userNick)) {
            throw new SecurityException("❌ 삭제 권한이 없습니다.");
        }

        // 1. 프로젝트와 관련된 모든 팀 신청 삭제
        userProjectRepository.deleteByProject(project);

        // 2. 프로젝트와 관련된 모든 알림 삭제
        notificationRepository.deleteByProject(project);

        // 프로젝트 삭제 전 히스토리 저장
        saveProjectHistory(project, "삭제됨");

        // 프로젝트 실제 삭제 (INACTIVE 상태로 변경하지 않고 삭제)
        projectRepository.delete(project);

        logger.info("✅ 프로젝트 삭제 완료 (ID={})");
    }

    /**
     * ✅ 프로젝트 수정/삭제 이력 저장 (히스토리 남기기)
     */
    @Transactional
    public void saveProjectHistory(ProjectEntity project, String actionType) {
        try {
            ProjectHistoryEntity history = ProjectHistoryEntity.builder()
                    .project(project)
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .goal(project.getGoal())
                    .createdBy(project.getCreatedBy())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .recruitmentPeriod(project.getRecruitmentPeriod())
                    .recruitmentCount(project.getRecruitmentCount())
                    .recruitmentEndDate(project.getRecruitmentEndDate())
                    .recruitmentStartDate(project.getRecruitmentStartDate())
                    .views(project.getViews())
                    .likes(project.getLikes())
                    .currentParticipants(project.getCurrentParticipants())
                    .modifiedAt(LocalDateTime.now())
                    .actionType(actionType)
                    .status(project.getStatus())  // 모집 상태도 저장
                    .build();

            projectHistoryRepository.save(history);  // 히스토리 저장
        } catch (Exception e) {
            logger.error("❌ 프로젝트 히스토리 저장 실패: " + e.getMessage());
            throw new RuntimeException("히스토리 저장 실패", e);
        }
    }

    /**
     * ✅ 프로젝트 업데이트 (수정 후 히스토리 저장)
     */
    @Transactional
    public ProjectEntity updateProject(Long id, String title, String description, String goal,
                                       LocalDate startDate, LocalDate endDate,
                                       LocalDate recruitmentStartDate, LocalDate recruitmentEndDate,
                                       int recruitmentPeriod, Integer recruitmentCount) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));

        project.setTitle(title);
        project.setDescription(description);
        project.setGoal(goal);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setRecruitmentPeriod(recruitmentPeriod);

        if (recruitmentStartDate != null) {
            project.setRecruitmentStartDate(recruitmentStartDate);
        }
        if (recruitmentEndDate != null) {
            project.setRecruitmentEndDate(recruitmentEndDate);
        }
        if (recruitmentCount != null) {
            project.setRecruitmentCount(recruitmentCount);
        }

        project.updateStatus(); // 업데이트 후 상태도 변경
        ProjectEntity updatedProject = projectRepository.save(project);
        saveProjectHistory(updatedProject, "수정됨");
        return updatedProject;
    }

    /**
     * ✅ 좋아요 토글 (좋아요 추가/삭제)
     */
    @Transactional
    public ProjectEntity toggleLike(Long projectId, Long userId) {
        ProjectEntity project = getProjectById(projectId);
        if (project.getLikedUsers().contains(userId)) {
            project.getLikedUsers().remove(userId);
            project.setLikes(project.getLikes() - 1);
        } else {
            project.getLikedUsers().add(userId);
            project.setLikes(project.getLikes() + 1);
        }
        return projectRepository.save(project);
    }

    /**
     * ✅ 조회수 증가
     */
    @Transactional
    public ProjectEntity incrementViews(Long projectId) {
        ProjectEntity project = getProjectById(projectId);
        project.setViews(project.getViews() + 1);
        return projectRepository.save(project);
    }

    /**
     * ✅ 프로젝트를 DTO로 변환하는 메서드
     */
    public ProjectDTO convertToDTO(ProjectEntity projectEntity) {
        // ✅ 모집중/진행중/완료 상태인 인원만 세기
        List<String> validStatuses = List.of("모집중", "진행중", "완료");
        int currentParticipants = userProjectRepository.countByProjectAndStatusIn(projectEntity, validStatuses);

        return new ProjectDTO(
                projectEntity.getId(),
                projectEntity.getTitle(),
                projectEntity.getCreatedBy(),
                projectEntity.getDescription(),
                projectEntity.getGoal(),
                projectEntity.getStartDate(),
                projectEntity.getEndDate(),
                projectEntity.getRecruitmentCount(), // 총 모집 인원
                currentParticipants,                 // 모집된 사람 수
                projectEntity.getViews(),
                projectEntity.getLikes(),
                projectEntity.getStatus(),
                projectEntity.getRecruitmentPeriod(),
                projectEntity.getRecruitmentStartDate(),
                projectEntity.getRecruitmentEndDate()
        );
    }


    /**
     * ✅ 사용자가 만든 프로젝트 목록을 반환
     */
    public List<ProjectDTO> getCreatedProjects(UserEntity user) {
        // 사용자가 만든 프로젝트 전체 조회
        List<ProjectEntity> createdProjects = projectRepository.findByCreatedBy(user.getUserNick());

        // 🔍 UserProjectEntity에서 visible=true인 것만 필터링
        return createdProjects.stream()
                .filter(project -> userProjectRepository.findByUserAndProject(user, project)
                        .map(UserProjectEntity::isVisible)  // visible이 true인 경우만 통과
                        .orElse(false))  // 없으면 false
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * ✅ 사용자가 참가한 프로젝트 목록을 반환
     */
    public List<ProjectDTO> getJoinedProjects(UserEntity user) {
        // "진행중" 또는 "완료" 상태의 프로젝트 조회, "모집중" 상태도 포함
        List<UserProjectEntity> userProjects = userProjectRepository.findByUserAndStatusIn(user, List.of("진행중", "완료", "모집중"));

        return userProjects.stream()
                .filter(UserProjectEntity::isVisible) // ✅ visible=true만 필터링
                .map(userProject -> userProject.getProject())
                .filter(project -> !project.getCreatedBy().equals(user.getUserNick())) // 주최자는 제외
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    public void applyForProject(Long projectId, UserEntity user) {
        ProjectEntity project = getProjectById(projectId);

        // ✅ 신청한 적이 있는지 확인 (쿼리 한 번으로 처리)
        if (userProjectRepository.existsByUserAndProject(user, project)) {
            throw new IllegalArgumentException("이미 신청한 프로젝트입니다.");
        }

        // ✅ 신청 정보 저장 (처음에는 "승인 대기" 상태)
        UserProjectEntity userProjectEntity = UserProjectEntity.builder()
                .user(user)
                .project(project)
                .joinDate(LocalDate.now())
                .status("신청중") // ✅ 처음에는 "승인 대기" 상태
                .build();
        userProjectRepository.save(userProjectEntity);

        // ✅ 프로젝트의 모집 인원 업데이트
        project.setCurrentParticipants(project.getCurrentParticipants() + 1);
        projectRepository.save(project);
    }

    // 프로젝트 신청 처리 메서드 추가
    public void submitApplication(UserEntity userEntity, ProjectEntity project, String message) {
        // 신청 정보 저장
        applyForProject(project.getId(), userEntity);

        // 신청 메세지 로직 추가 (필요한 경우)
        // 예: 신청 메시지를 저장하거나 추가적인 처리 수행
    }


    // 신청 수락 로직
    @Transactional
    public void acceptTeamRequest(Long projectId, Long userId, UserEntity hostUser) {
        ProjectEntity project = getProjectById(projectId);

        if (!project.getCreatedBy().equals(hostUser.getUserNick())) {
            throw new IllegalArgumentException("❌ 프로젝트 생성자만 신청을 수락할 수 있습니다.");
        }

        UserEntity applicant = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 사용자입니다."));

        UserProjectEntity userProject = userProjectRepository.findByUserAndProject(applicant, project)
                .orElseThrow(() -> new IllegalArgumentException("❌ 신청 내역이 없습니다."));

        // 주최자의 상태를 확인하여 팀원 상태 업데이트
        String hostStatus = project.getUserProjects().stream()
                .filter(up -> up.getUser().getUserNick().equals(project.getCreatedBy()))
                .map(UserProjectEntity::getStatus)
                .findFirst()
                .orElse("모집중");

        userProject.setStatus(hostStatus);  // 주최자의 상태로 팀원 상태 설정
        userProjectRepository.save(userProject);

        // 프로젝트의 상태도 업데이트
        project.updateStatus();
        projectRepository.save(project);

        // ✅ 알림 삭제 (receiver는 hostUser 자체로 넘겨야 함)
        notificationRepository.deleteBySenderAndReceiverAndProjectTitle(
                applicant.getUserNick(),   // sender 닉네임
                hostUser,                  // receiver (UserEntity 타입)
                project.getTitle()         // 프로젝트 제목
        );

        // 주최자가 "완료" 상태로 변경 시, 팀원들도 완료로 업데이트해야 함 (다음 단계에서)
        // 완료 상태는 파일 제출 후에만 변경될 수 있도록 로직 분리
    }




    // 신청 거절 로직
    @Transactional
    public void rejectTeamRequest(Long projectId, Long userId, UserEntity hostUser) {
        // 프로젝트 조회
        ProjectEntity project = getProjectById(projectId);

        // 신청한 사용자 조회
        UserEntity applicant = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 사용자입니다."));

        // 신청 내역 가져오기
        UserProjectEntity userProject = userProjectRepository.findByUserAndProject(applicant, project)
                .orElseThrow(() -> new IllegalArgumentException("❌ 신청 내역이 없습니다."));

        // 상태를 "거절됨"으로 변경
        userProject.setStatus("거절됨");
        userProjectRepository.save(userProject);

        // ✅ 알림 삭제 (receiver는 hostUser 자체로 넘겨야 함)
        notificationRepository.deleteBySenderAndReceiverAndProjectTitle(
                applicant.getUserNick(),   // sender 닉네임
                hostUser,                  // receiver (UserEntity)
                project.getTitle()         // 프로젝트 제목
        );
    }



    @Transactional
    public void completeProjectInService(Long projectId) {
        // 프로젝트 찾기
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 프로젝트를 찾을 수 없습니다."));

        // 파일 제출이 있어야만 "완료" 상태로 변경되므로, 파일이 제출된 경우에만 완료 상태로 변경
        if (project.getUserProjects().stream().anyMatch(up -> up.getStatus().equals("완료") && up.getSubmittedFileName() != null)) {
            // 프로젝트 상태를 '완료'로 변경
            project.setStatus("완료");

            // 참여한 팀원들의 상태도 '완료'로 업데이트
            updateProjectCompletionStatus(projectId);
        }

        projectRepository.save(project);  // 상태 업데이트 후 프로젝트 저장
    }

    // 프로젝트 상태 변경 (주최자가 완료로 변경 시 팀원들도 완료로)
    @Transactional
    public void updateProjectCompletionStatus(Long projectId) {
        ProjectEntity project = getProjectById(projectId);

        if ("완료".equals(project.getStatus())) {
            List<UserProjectEntity> userProjects = userProjectRepository.findByProject(project);

            for (UserProjectEntity userProject : userProjects) {
                if ("진행중".equals(userProject.getStatus()) || "모집중".equals(userProject.getStatus())) {
                    userProject.setStatus("완료");
                    userProjectRepository.save(userProject);
                }
            }
        }
    }



    @Transactional
    public void sendTeamRequestNotification(Long projectId, String userNick) {
        // 프로젝트 정보 가져오기
        ProjectEntity project = getProjectById(projectId);

        // 신청자 정보 가져오기 (userNick으로 조회)
        UserEntity requester = userRepository.findByUserNick(userNick)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        // 프로젝트 생성자(작성자) 정보를 가져오는 코드
        UserEntity projectOwner = userRepository.findByUserNick(project.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Project owner not found"));


        // 알림 메시지 생성
        String message = requester.getUserNick() + "님이 '" + project.getTitle() + "' 프로젝트에 팀원 신청을 보냈습니다!";

        // 알림 객체 생성
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(projectOwner);  // 알림을 받을 사용자: 프로젝트 생성자
        notification.setSender(requester);  // 알림을 보낸 사람: 팀 신청자
        notification.setMessage(message);
        notification.setProject(project);  // 해당 프로젝트
        notification.setTimestamp(LocalDateTime.now());
        notification.setIsRead(false);  // 알림은 처음에는 읽지 않은 상태

        // 알림 저장
        notificationRepository.save(notification);
    }

    public void submitProjectFile(UserEntity user, ProjectEntity project, String fileName, MultipartFile file) {
        logger.debug("제출할 파일명: " + fileName);

        // 주최자는 UserProjectEntity가 존재하지 않으므로, 이를 체크하여 예외처리
        UserProjectEntity userProject = userProjectRepository.findByUser_UserIdAndProject_Id(user.getUserId(), project.getId())
                .orElseGet(() -> {
                    if (user.getUserNick().equals(project.getCreatedBy())) {
                        // 주최자의 경우 UserProjectEntity를 생성하지 않으므로 null로 반환
                        return null;
                    } else {
                        throw new IllegalArgumentException("❌ 해당 프로젝트에 대한 신청 정보가 없습니다.");
                    }
                });

        if (userProject == null) {
            // 주최자는 파일을 제출하는 경우만, 바로 상태 변경하고 파일 처리
            userProject = new UserProjectEntity();
            userProject.setUser(user);
            userProject.setProject(project);
            userProject.setStatus("완료");  // 기본 상태 설정 (필요에 따라 다르게 설정)
        }

        // 제출 날짜 및 파일명 저장
        userProject.setSubmissionDate(LocalDate.now());
        userProject.setSubmittedFileName(fileName);

        // 파일 경로 확인
        Path directoryPath = Paths.get(projectFilePath);
        Path filePath = directoryPath.resolve(fileName);

        try {
            // 파일을 지정된 경로에 저장
            file.transferTo(filePath);
            logger.debug("파일이 성공적으로 저장되었습니다: " + filePath.toString());
        } catch (Exception e) {
            logger.error("파일 저장 중 오류 발생: " + e.getMessage(), e);
            return;
        }

        // DB에 저장
        userProjectRepository.save(userProject);
        logger.debug("DB에 저장 완료: " + userProject.getSubmittedFileName());

        // **모집중**, **진행중**, **완료** 상태인 팀원들에게도 파일명과 제출 날짜 업데이트
        updateTeamMembersSubmission(project);  // 팀원들에게도 파일명과 제출 날짜를 업데이트

        // 파일 제출 후 프로젝트 상태를 "완료"로 변경
        completeProjectInService(project.getId());
    }

    // 팀원들에게 제출 정보 업데이트
    @Transactional
    public void updateTeamMembersSubmission(ProjectEntity project) {
        List<UserProjectEntity> userProjects = userProjectRepository.findByProject(project);

        for (UserProjectEntity userProject : userProjects) {
            // "모집중", "진행중", "완료" 상태인 팀원만 업데이트
            if ("모집중".equals(userProject.getStatus()) ||
                    "진행중".equals(userProject.getStatus()) ||
                    "완료".equals(userProject.getStatus())) {

                // 제출 파일명과 제출 날짜 업데이트
                userProject.setSubmittedFileName(project.getUserProjects().stream()
                        .filter(up -> up.getUser().getUserNick().equals(project.getCreatedBy()))  // 주최자
                        .map(UserProjectEntity::getSubmittedFileName)
                        .findFirst().orElse(null));  // 주최자의 제출 파일명

                userProject.setSubmissionDate(LocalDate.now()); // 제출 날짜 업데이트

                // 팀원 상태를 "완료"로 변경
                userProject.setStatus("완료");

                userProjectRepository.save(userProject); // 팀원에게 업데이트된 정보 저장
            }
        }
    }
    // 주최자 닉네임을 기준으로 UserEntity를 반환하는 메서드
    public UserEntity getUserByNick(String userNick) {
        return userRepository.findByUserNick(userNick)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 닉네임을 가진 사용자가 없습니다. userNick=" + userNick));


    }

    /**
     * ✅ 프로젝트 공지사항 조회 (읽기 전용)
     */
    @Transactional(readOnly = true)
    public String getNotice(Long projectId) {
        // 프로젝트 ID로 프로젝트 조회
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다."));
        // 공지사항 내용 반환
        return project.getNotice();
    }

    /**
     * ✅ 프로젝트 공지사항 수정 (팀장만 가능)
     */
    @Transactional
    public void updateNotice(Long projectId, String content, UserEntity user) {
        // 프로젝트 ID로 프로젝트 조회
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다."));

        // 공지사항은 팀장(프로젝트 생성자)만 수정 가능
        if (!project.getCreatedBy().equals(user.getUserNick())) {
            throw new SecurityException("공지사항 수정 권한이 없습니다.");
        }

        // 공지사항 내용 수정
        project.setNotice(content);
        // DB에 저장
        projectRepository.save(project);
    }

    public Map<String, Object> getProjectMembersInfo(String title, UserEntity currentUser) {
        Map<String, Object> response = new HashMap<>();

        // 프로젝트 가져오기
        ProjectEntity project = projectRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다: " + title));

        // 주최자
        String creator = project.getCreatedBy();
        response.put("creator", creator);

        // 현재 로그인한 사용자 닉네임
        response.put("currentUser", currentUser.getUserNick());

        // 팀원 닉네임 리스트 가져오기
        List<String> members = userProjectRepository.findByProject(project).stream()
                .map(up -> up.getUser().getUserNick())
                .collect(Collectors.toList());
        response.put("members", members);

        return response;
    }
    // 🔹 ProjectService.java 가장 아래에 추가하면 돼!
    public List<String> getProjectMemberNicknames(String projectTitle) {
        ProjectEntity project = projectRepository.findByTitle(projectTitle)
                .orElseThrow(() -> new IllegalArgumentException("❌ 프로젝트를 찾을 수 없습니다: " + projectTitle));

        List<String> memberNicks = userProjectRepository.findByProject(project).stream()
                .map(up -> up.getUser().getUserNick())
                .collect(Collectors.toList());

        // 주최자가 목록에 없으면 추가
        if (!memberNicks.contains(project.getCreatedBy())) {
            memberNicks.add(project.getCreatedBy());
        }

        return memberNicks;
    }

    // 🔹 이것도 ProjectService.java에 추가
    public boolean isUserHost(String projectTitle, String userNick) {
        ProjectEntity project = projectRepository.findByTitle(projectTitle)
                .orElseThrow(() -> new IllegalArgumentException("❌ 프로젝트를 찾을 수 없습니다: " + projectTitle));
        return project.getCreatedBy().equals(userNick);
    }





}







