package com.example.bob.Service;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.ProjectRepository;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    public void saveProject(ProjectEntity project) {
        // 현재 로그인된 사용자의 닉네임 가져오기
        String creatorNick = getCurrentUserNickname();
        if (creatorNick == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        // 생성자 닉네임 설정
        project.setCreatedBy(creatorNick);

        // 프로젝트 저장
        projectRepository.save(project);
    }

    private String getCurrentUserNickname() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getUserEntity().getUserNick(); // 로그인된 사용자의 닉네임 반환
        }
        return null;
    }
}
