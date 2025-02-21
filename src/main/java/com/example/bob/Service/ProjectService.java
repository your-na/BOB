package com.example.bob.Service;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    // 프로젝트 목록 가져오기
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    // 특정 프로젝트 가져오기
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다: " + id));
    }

    // 좋아요 토글 (좋아요 추가/삭제)
    // ProjectService.java

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


    // 조회수 증가
    @Transactional
    public ProjectEntity incrementViews(Long projectId) {
        ProjectEntity project = getProjectById(projectId);
        project.setViews(project.getViews() + 1);
        return projectRepository.save(project);
    }

    // 프로젝트 저장 후 프로젝트 반환
    public ProjectEntity saveProject(ProjectEntity project) {
        return projectRepository.save(project);
    }
}
