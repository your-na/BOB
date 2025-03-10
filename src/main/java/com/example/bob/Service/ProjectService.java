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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository projectHistoryRepository;
    private final UserProjectRepository userProjectRepository; // UserProjectRepository 추가

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @PersistenceContext
    private EntityManager entityManager;  // EntityManager 주입

    /**
     * ✅ 모든 프로젝트를 DTO로 변환하여 반환
     */
    public List<ProjectDTO> getAllProjectsDTO() {
        return projectRepository.findAllActiveProjects().stream()  // INACTIVE 상태 제외
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
    public ProjectEntity saveProject(ProjectEntity project) {
        logger.info("🚀 프로젝트 저장 전 모집 종료일: {}", project.getRecruitmentEndDate()); // 로그 추가

        project.updateStatus(); // 상태 업데이트
        ProjectEntity savedProject = projectRepository.save(project);

        logger.info("✅ 저장된 프로젝트의 모집 종료일: {}", savedProject.getRecruitmentEndDate()); // 로그 추가

        saveProjectHistory(savedProject, "생성됨");
        return savedProject;
    }

    /**
     * ✅ 프로젝트 삭제 (히스토리 유지)
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

        // 프로젝트 삭제 이력 저장
        saveProjectHistory(project, "삭제됨");

        // 실제 삭제하는 대신 상태를 "INACTIVE"로 변경
        project.setStatus("INACTIVE");
        projectRepository.save(project);

        logger.info("✅ 프로젝트 비활성화 완료 (ID={})", id);
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

            projectHistoryRepository.save(history);
            entityManager.flush(); // 즉시 DB 반영
            entityManager.clear(); // Hibernate가 DELETE 시 히스토리를 날리는 것 방지

            logger.info("✅ 프로젝트 히스토리 저장됨: " + history);

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
        return new ProjectDTO(
                projectEntity.getId(),
                projectEntity.getTitle(),
                projectEntity.getCreatedBy(),
                projectEntity.getDescription(),
                projectEntity.getGoal(),
                projectEntity.getStartDate(),
                projectEntity.getEndDate(),
                projectEntity.getRecruitmentCount(),
                projectEntity.getCurrentParticipants(),
                projectEntity.getViews(),
                projectEntity.getLikes(),
                projectEntity.getStatus(),  // 한글 상태 반영
                projectEntity.getRecruitmentPeriod(),
                projectEntity.getRecruitmentStartDate(),
                projectEntity.getRecruitmentEndDate()
        );
    }

    /**
     * ✅ 사용자가 만든 프로젝트 목록을 반환
     */
    public List<ProjectDTO> getCreatedProjects(UserEntity user) {
        // UserEntity를 사용하여 사용자가 만든 프로젝트 목록을 조회
        List<ProjectEntity> createdProjects = projectRepository.findByCreatedBy(user.getUserNick());
        return createdProjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 사용자가 참가한 프로젝트 목록을 반환
     */
    public List<ProjectDTO> getJoinedProjects(UserEntity user) {
        // UserEntity를 사용하여 사용자가 참가한 프로젝트 목록을 조회
        List<UserProjectEntity> userProjects = userProjectRepository.findByUser(user);
        return userProjects.stream()
                .map(userProject -> convertToDTO(userProject.getProject()))
                .collect(Collectors.toList());
    }
}
