package com.example.bob.Service;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    /**
     * ✅ 모든 프로젝트 가져오기
     */
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * ✅ 프로젝트 저장 후, 저장된 프로젝트 반환
     */
    public ProjectEntity saveProject(ProjectEntity project) {
        return projectRepository.save(project);
    }

    /**
     * ✅ 특정 프로젝트 가져오기 (ID 기반)
     */
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + id));
    }

    /**
     * ✅ 프로젝트 좋아요 토글 (추가 / 취소)
     */
    @Transactional
    public ProjectEntity toggleLike(Long projectId, Long userId) {
        ProjectEntity project = getProjectById(projectId);

        // 이미 좋아요를 눌렀다면 취소
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
     * ✅ 스크랩 토글 (추가 / 취소)
     */
    @Transactional
    public ProjectEntity toggleScrap(Long projectId, Long userId) {
        ProjectEntity project = getProjectById(projectId);

        if (project.getScrapUsers().contains(userId)) {
            project.getScrapUsers().remove(userId);
        } else {
            project.getScrapUsers().add(userId);
        }

        return projectRepository.save(project);
    }

    /**
     * ✅ 프로젝트 목록 갱신 (새 프로젝트 저장 후 목록 자동 업데이트)
     */
    public List<ProjectEntity> refreshProjectList() {
        return projectRepository.findAll();
    }
}
