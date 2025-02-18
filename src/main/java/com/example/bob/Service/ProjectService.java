package com.example.bob.Service;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
