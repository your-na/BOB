package com.example.bob.Controller;

import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Repository.UserProjectRepository;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AppStatusController {

    private final UserProjectRepository userProjectRepository;

    @GetMapping("/appstatus")
    public String showApplicationStatus(Model model,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserEntity currentUser = userDetails.getUserEntity(); // 🔥 여기 주목!

        List<UserProjectEntity> projectList =
                userProjectRepository.findByUserAndStatus(currentUser, "신청중");

        model.addAttribute("projectApplications", projectList);
        return "application_status";
    }
}
