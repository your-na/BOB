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
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
                projectEntity.getRecruitmentPeriod()
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
        return projectRepository.save(project);
    }

    @PersistenceContext
    private EntityManager entityManager;  // EntityManager 주입

    /**
     * 프로젝트 수정/삭제 이력 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
                    .modifiedAt(LocalDateTime.now())
                    .actionType(actionType)
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

    /**
     * 프로젝트 수정 (트랜잭션 적용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProjectEntity updateProject(Long id, String title, String description, String goal,
                                       LocalDate startDate, LocalDate endDate, int recruitmentPeriod) {
        System.out.println("✅ updateProject 시작");

        // 프로젝트 조회
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));
        System.out.println("✅ 프로젝트 조회 완료: " + project.getId());

        // 프로젝트 정보 업데이트 전에 히스토리 저장
        System.out.println("✅ 히스토리 저장 함수 호출 전");
        saveProjectHistory(project, "수정됨");
        System.out.println("✅ 히스토리 저장 함수 호출 후");

        // 프로젝트 정보 업데이트
        project.setTitle(title);
        project.setDescription(description);
        project.setGoal(goal);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setRecruitmentPeriod(recruitmentPeriod);

        System.out.println("✅ 프로젝트 정보 업데이트 완료");

        // 업데이트된 프로젝트를 프로젝트 테이블에 저장
        project = projectRepository.save(project);
        System.out.println("✅ 프로젝트 저장 완료");

        // 수정 후 저장된 프로젝트 리턴
        return project;
    }

    /**
     * 프로젝트 삭제 (논리 삭제 X, 실제 DB에서 제거)
     */
    @Transactional
    public void deleteProject(Long id) {
        try {
            // 1️⃣ 삭제할 프로젝트 찾기
            ProjectEntity project = projectRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));

            // 2️⃣ 삭제 이력 저장 (프로젝트 히스토리에 남기기)
            saveProjectHistory(project, "삭제됨");

            // 3️⃣ 실제 프로젝트 삭제 (DB에서 제거)
            projectRepository.deleteById(id);

            System.out.println("🔥 프로젝트 삭제 완료: " + id);
        } catch (Exception e) {
            logger.error("❌ 프로젝트 삭제 중 오류 발생: ", e);
            throw e;  // 예외 발생 시 롤백
        }
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
}
