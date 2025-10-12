package com.example.bob.Service;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JjimProjectService {

    private final ProjectRepository projectRepository;

    // ✅ 로그인한 유저가 찜한 프로젝트만 가져오기
    public List<ProjectDTO> findJjimProjectsByUser(Long userId) {
        List<ProjectEntity> projects = projectRepository.findByLikedUsersContaining(userId);

        return projects.stream().map(this::convertToDTO).toList();
    }

    // ✅ DTO로 변환
    private ProjectDTO convertToDTO(ProjectEntity project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .createdBy(project.getCreatorNick()) // 또는 getCreatedBy()
                .recruitmentStartDate(project.getRecruitmentStartDate())
                .recruitmentEndDate(project.getRecruitmentEndDate())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .views(project.getViews())
                .likes(project.getLikes())
                .recruitmentCount(project.getRecruitmentCount())
                .currentParticipants(project.getCurrentParticipants())
                .status(project.getStatus())
                .build();
    }
}
