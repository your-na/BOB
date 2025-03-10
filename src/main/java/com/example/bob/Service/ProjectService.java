package com.example.bob.Service;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.ProjectHistoryEntity;
import com.example.bob.Repository.ProjectHistoryRepository;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<< Updated upstream
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Propagation;


import jakarta.persistence.EntityManager;
=======

>>>>>>> Stashed changes
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
<<<<<<< Updated upstream
=======

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
>>>>>>> Stashed changes

@Service
@RequiredArgsConstructor
@PersistenceContext

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository projectHistoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

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
                projectEntity.getStatus(),
                projectEntity.getRecruitmentPeriod(),
                projectEntity.getRecruitmentStartDate(),
                projectEntity.getRecruitmentEndDate()

        );
    }

    /**
     * ✅ 모든 프로젝트를 DTO로 변환하여 반환
     */
    public List<ProjectDTO> getAllProjectsDTO() {
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 특정 프로젝트 가져오기
     */
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + id));
    }

    /**
     * ✅ 프로젝트 저장 후 반환
     */
    @Transactional
    public ProjectEntity saveProject(ProjectEntity project) {
<<<<<<< Updated upstream
        return projectRepository.save(project);
    }

    @PersistenceContext
    private EntityManager entityManager;  // EntityManager 주입
=======
        logger.info("🚀 프로젝트 저장 전 모집 종료일: {}", project.getRecruitmentEndDate()); // 🚀 로그 추가

        project.updateStatus(); // ✅ 상태 업데이트
        ProjectEntity savedProject = projectRepository.save(project);

        logger.info("✅ 저장된 프로젝트의 모집 종료일: {}", savedProject.getRecruitmentEndDate()); // 🚀 로그 추가

        saveProjectHistory(savedProject, "생성됨");
        return savedProject;
    }


    /**
     * ✅ 프로젝트 삭제 (히스토리 유지)
     */
    @Transactional
    public void deleteProject(Long id, String userNick) {
        // ✅ 프로젝트 찾기
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 프로젝트가 없습니다."));

        String owner = project.getCreatedBy();
        if (!owner.equals(userNick)) {
            throw new SecurityException("❌ 삭제 권한이 없습니다.");
        }

        // ✅ 프로젝트 삭제 이력 저장
        saveProjectHistory(project, "삭제됨");

        // ✅ 실제 삭제하는 대신 상태를 "INACTIVE"로 변경
        project.setStatus("INACTIVE");
        projectRepository.save(project);

        logger.info("✅ 프로젝트 비활성화 완료 (ID={})", id);
    }
>>>>>>> Stashed changes

    /**
     * 프로젝트 수정/삭제 이력 저장
     */
    @Transactional
    public void saveProjectHistory(ProjectEntity project, String actionType) {
        try {
            // 이력 객체 생성
            ProjectHistoryEntity history = ProjectHistoryEntity.builder()
                    .project(project)
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .goal(project.getGoal())
                    .createdBy(project.getCreatedBy())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .recruitmentPeriod(project.getRecruitmentPeriod())
                    .recruitmentCount(project.getRecruitmentCount()) // ✅ 모집 인원 추가
                    .recruitmentEndDate(project.getRecruitmentEndDate())
                    .recruitmentStartDate(project.getRecruitmentStartDate())
                    .modifiedAt(LocalDateTime.now())
                    .actionType(actionType)
                    .status(project.getStatus())  // ✅ 모집 상태도 저장
                    .build();

            // 디버깅: 저장될 히스토리 값 출력
            logger.info("Saving project history: " + history);

            // EntityManager를 사용하여 저장
            entityManager.persist(history);  // 엔티티 저장
            entityManager.flush();  // 즉시 커밋
        } catch (Exception e) {
            logger.error("프로젝트 히스토리 저장 실패: " + e.getMessage());
            throw new RuntimeException("히스토리 저장 실패", e);  // 예외 발생 시 롤백 유도
        }
    }
    @Transactional
    public ProjectEntity updateProject(Long id, String title, String description, String goal,
                                       LocalDate startDate, LocalDate endDate,
                                       LocalDate recruitmentStartDate, LocalDate recruitmentEndDate,
                                       int recruitmentPeriod, Integer recruitmentCount) { // ✅ Integer로 변경
        System.out.println("✅ updateProject 시작");

        // 프로젝트 조회
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));
        System.out.println("✅ 프로젝트 조회 완료: " + project.getId());

        // 기존 값 확인
        System.out.println("🔥 기존 모집 일정: 시작일=" + project.getRecruitmentStartDate() + ", 종료일=" + project.getRecruitmentEndDate());
        System.out.println("🔥 기존 모집 인원: " + project.getRecruitmentCount());

        // 프로젝트 정보 업데이트
        project.setTitle(title);
        project.setDescription(description);
        project.setGoal(goal);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setRecruitmentPeriod(recruitmentPeriod);

        // ✅ 모집 일정 변경 로그 추가
        if (recruitmentStartDate != null && !recruitmentStartDate.equals(project.getRecruitmentStartDate())) {
            System.out.println("✅ 모집 시작일 변경: " + project.getRecruitmentStartDate() + " → " + recruitmentStartDate);
            project.setRecruitmentStartDate(recruitmentStartDate);
        }

        if (recruitmentEndDate != null && !recruitmentEndDate.equals(project.getRecruitmentEndDate())) {
            System.out.println("✅ 모집 종료일 변경: " + project.getRecruitmentEndDate() + " → " + recruitmentEndDate);
            project.setRecruitmentEndDate(recruitmentEndDate);
        }

        // ✅ 모집 인원 변경 확인 및 업데이트 (기본형 int 비교)
        if (recruitmentCount != null && project.getRecruitmentCount() != recruitmentCount) {
            System.out.println("✅ 모집 인원 변경: " + project.getRecruitmentCount() + " → " + recruitmentCount);
            project.setRecruitmentCount(recruitmentCount);
        } else {
            System.out.println("⚠ 모집 인원 변경 없음: " + project.getRecruitmentCount());
        }

<<<<<<< Updated upstream
        System.out.println("✅ 프로젝트 정보 업데이트 완료");

        // 🚀 강제 저장 (DB 반영 확인)
        project = projectRepository.save(project);
        projectRepository.flush(); // ✅ DB 즉시 반영

        System.out.println("🔥 최종 저장된 모집 인원: " + project.getRecruitmentCount());

        // ✅ 프로젝트 정보 업데이트 후 히스토리 저장
        saveProjectHistory(project, "수정됨");

        return project;
    }

    /**
     * 프로젝트 삭제 (논리 삭제 X, 실제 DB에서 제거)
     */
    @Transactional
    public void deleteProject(Long id, String userNick) {  // ✅ userNick 추가
        try {
            ProjectEntity project = projectRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("❌ 해당 프로젝트가 없습니다."));

            // ✅ 프로젝트 작성자와 요청 사용자가 같은지 확인
            if (!project.getCreatedBy().equals(userNick)) {
                throw new SecurityException("❌ 삭제 권한이 없습니다."); // 🔴 예외 추가
            }

            // ✅ 삭제 전에 히스토리 저장
            saveProjectHistory(project, "삭제됨");

            // ✅ 프로젝트 삭제
            projectRepository.deleteById(id);
        } catch (SecurityException e) {
            logger.error("🚨 삭제 권한 없음: " + e.getMessage());
            throw e;  // ❗ 사용자 권한 문제는 그대로 던짐
        } catch (Exception e) {
            logger.error("❌ 프로젝트 삭제 중 오류 발생: ", e);
            throw new RuntimeException("프로젝트 삭제 실패", e);  // ❗ 명확한 예외 메시지 던짐
        }
    }



=======
        project.updateStatus(); // ✅ 업데이트 후 상태도 변경
        ProjectEntity updatedProject = projectRepository.save(project);
        saveProjectHistory(updatedProject, "수정됨");
        return updatedProject;
    }

    /**
     * ✅ 기존 ACTIVE 데이터를 "모집중" 또는 "진행중"으로 변경
     */
    @Transactional
    public void updateOldStatuses() {
        LocalDate today = LocalDate.now();
        projectRepository.updateOldActiveToRecruiting(today);
        projectRepository.updateOldActiveToOngoing(today);
    }

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======

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
                projectEntity.getStatus(),  // ✅ 한글 상태 반영
                projectEntity.getRecruitmentPeriod(),
                projectEntity.getRecruitmentStartDate(),
                projectEntity.getRecruitmentEndDate()
        );
    }
>>>>>>> Stashed changes
}
