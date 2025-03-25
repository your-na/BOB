package com.example.bob.Controller;

import com.example.bob.Entity.UserEntity;
import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Service.ProjectService;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@Controller
public class SpaceController {

    @Autowired
    private ProjectService projectService;

    // 프로젝트 목록
    @GetMapping("/plan")
    public String getUserProjects(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity userEntity = userDetails.getUserEntity();

        // 사용자가 주최한 프로젝트 목록
        List<ProjectDTO> createdProjects = projectService.getCreatedProjects(userEntity);

        // 사용자가 참여한 프로젝트 목록
        List<ProjectDTO> joinedProjects = projectService.getJoinedProjects(userEntity);

        // 모델에 프로젝트 데이터를 추가하여 전달
        model.addAttribute("createdProjects", createdProjects);
        model.addAttribute("joinedProjects", joinedProjects);

        return "plan"; // plan.html 템플릿 렌더링
    }

    // 할 일 팝업 (task_popup.html)
    @GetMapping("/popup")
    public String openTaskPopup(Model model) {
        return "task_popup"; // task_popup.html 템플릿 렌더링
    }
}

