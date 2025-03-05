package com.example.bob.Service;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.ProjectHistoryEntity;
import com.example.bob.Repository.ProjectHistoryRepository;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository projectHistoryRepository;

    /**
     * ✅ 프로젝트를 DTO로 변환하는 메서드
     */
    public ProjectDTO convertToDTO(ProjectEntity projectEntity) {
        return new ProjectDTO(
                projectEntity.getId(),
                projectEntity.getTitle(),
                projectEntity.getCreatedBy(),
                projectEntity.getDescription(), // ✅ 추가됨 (프로젝트 내용)
                projectEntity.getGoal(), // ✅ 추가됨 (프로젝트 목표)
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
        List<ProjectEntity> projectEntities = projectRepository.findAll();
        return projectEntities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 모든 프로젝트 가져오기
     */
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
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
    public ProjectEntity saveProject(ProjectEntity project) {
        return projectRepository.save(project);
    }

    /**
     * ✅ 프로젝트 수정/삭제 이력 저장
     */
    public void saveProjectHistory(ProjectEntity project, String actionType) {
        ProjectHistoryEntity history = ProjectHistoryEntity.builder()
                .project(project)  // ✅ 연관된 프로젝트 저장
                .title(project.getTitle())
                .description(project.getDescription())
                .goal(project.getGoal())
                .createdBy(project.getCreatedBy())
                .startDate(project.getStartDate())  // ✅ 진행 시작일 저장
                .endDate(project.getEndDate())  // ✅ 진행 종료일 저장
                .recruitmentPeriod(project.getRecruitmentPeriod())  // ✅ 모집 기간 저장
                .modifiedAt(LocalDateTime.now())  // ✅ 수정 날짜 기록
                .actionType(actionType)  // "수정됨" 또는 "삭제됨"
                .build();

        projectHistoryRepository.save(history);  // ✅ 히스토리 저장
    }

    /**
     * ✅ 프로젝트 수정
     */
    @Transactional
    public ProjectEntity updateProject(Long id, String title, String description, String goal,
                                       LocalDate startDate, LocalDate endDate, int recruitmentPeriod) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));

        saveProjectHistory(project, "수정됨");  // ✅ 수정 전 데이터를 히스토리에 저장

        project.setTitle(title);
        project.setDescription(description);
        project.setGoal(goal);
        project.setStartDate(startDate);  // ✅ 진행 시작일 변경
        project.setEndDate(endDate);  // ✅ 진행 종료일 변경
        project.setRecruitmentPeriod(recruitmentPeriod);  // ✅ 모집 일정 변경

        return projectRepository.save(project);  // ✅ 변경된 데이터 저장
    }

    /**
     * ✅ 프로젝트 삭제 (논리 삭제 X, 실제 DB에서 제거)
     */
    @Transactional
    public void deleteProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 없습니다."));

        saveProjectHistory(project, "삭제됨");  // ✅ 삭제 이력 저장
        projectRepository.deleteById(id);  // ✅ 실제 테이블에서 삭제
    }

    /**
     * ✅ 좋아요 토글 (좋아요 추가/삭제)
     */
    @Transactional
    public ProjectEntity toggleLike(Long projectId, Long userId) {
        // 프로젝트를 가져옴
        ProjectEntity project = getProjectById(projectId);

        // 이미 좋아요를 눌렀으면 취소, 아니면 좋아요 추가
        if (project.getLikedUsers().contains(userId)) {
            project.getLikedUsers().remove(userId); // 좋아요 취소
            project.setLikes(project.getLikes() - 1); // 좋아요 수 감소
        } else {
            project.getLikedUsers().add(userId); // 좋아요 추가
            project.setLikes(project.getLikes() + 1); // 좋아요 수 증가
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