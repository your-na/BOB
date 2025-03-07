package com.example.bob.Service;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.ProjectHistoryEntity;
import com.example.bob.Repository.ProjectHistoryRepository;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
     * ✅ 프로젝트 히스토리에 기존 데이터를 저장하는 메서드
     */
    public void saveProjectHistory(ProjectEntity project, String actionType) {
        System.out.println("🔹 프로젝트 히스토리 저장 중... 프로젝트 제목: " + project.getTitle());

        ProjectHistoryEntity history = ProjectHistoryEntity.builder()
                .project(project)
                .title(project.getTitle())
                .description(project.getDescription())
                .goal(project.getGoal())
                .createdBy(project.getCreatedBy())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .recruitmentCount(project.getRecruitmentCount()) // ✅ 모집 인원 추가
                .recruitmentPeriod(project.getRecruitmentPeriod()) // ✅ 모집 기간 저장
                .recruitmentStartDate(project.getRecruitmentStartDate()) // ✅ 모집 시작일 저장
                .recruitmentEndDate(project.getRecruitmentEndDate()) // ✅ 모집 종료일 저장
                .modifiedAt(LocalDateTime.now())
                .actionType(actionType) // "수정됨" 또는 "삭제됨"
                .build();

        projectHistoryRepository.save(history);
        System.out.println("✅ 프로젝트 히스토리 저장 완료!");
    }

    /**
     * ✅ 프로젝트 수정
     */
    @Transactional
    public ProjectEntity updateProject(Long id, String title, String description, String goal,
                                       LocalDate startDate, LocalDate endDate,
                                       int recruitmentCount, int recruitmentPeriod,
                                       LocalDate recruitmentStartDate, LocalDate recruitmentEndDate) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트가 없습니다."));

        saveProjectHistory(project, "수정됨"); // ✅ 기존 데이터 저장

        // ✅ 프로젝트 데이터 업데이트
        project.setTitle(title);
        project.setDescription(description);
        project.setGoal(goal);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setRecruitmentCount(recruitmentCount); // ✅ 모집 인원 업데이트
        project.setRecruitmentPeriod(recruitmentPeriod); // ✅ 모집 기간 업데이트
        project.setRecruitmentStartDate(recruitmentStartDate); // ✅ 모집 시작일 업데이트
        project.setRecruitmentEndDate(recruitmentEndDate); // ✅ 모집 종료일 업데이트

        return projectRepository.save(project); // ✅ 최신 데이터 저장
    }

    /**
     * ✅ 프로젝트 삭제 (삭제 내역 저장)
     */
    @Transactional
    public void deleteProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트가 없습니다."));

        saveProjectHistory(project, "삭제됨"); // ✅ 삭제 이력 저장
        projectRepository.deleteById(id); // ✅ 실제 테이블에서 삭제
    }

    /**
     * ✅ 프로젝트 DTO 변환
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
                projectEntity.getRecruitmentCount(), // ✅ 모집 인원 추가
                projectEntity.getCurrentParticipants(),
                projectEntity.getViews(),
                projectEntity.getLikes(),
                projectEntity.getStatus(),
                projectEntity.getRecruitmentPeriod(),
                projectEntity.getRecruitmentStartDate(), // ✅ 추가됨
                projectEntity.getRecruitmentEndDate() // ✅ 추가됨
        );
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트가 존재하지 않습니다: " + id));
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
     * ✅ 좋아요 기능
     */
    @Transactional
    public ProjectEntity toggleLike(Long projectId, Long userId) {
        ProjectEntity project = getProjectById(projectId);
        return projectRepository.save(project);
    }

    /**
     * ✅ 프로젝트 저장
     */
    @Transactional
    public ProjectEntity saveProject(ProjectEntity project) {
        return projectRepository.save(project);
    }
}
