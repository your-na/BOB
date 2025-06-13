package com.example.bob.Controller;

import com.example.bob.Entity.BasicInfo;
import com.example.bob.Entity.Education;
import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Repository.BasicInfoRepository;
import com.example.bob.Repository.EducationRepository;
import com.example.bob.Repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequiredArgsConstructor
public class ResumeHistoryController {

    private final BasicInfoRepository basicInfoRepository;
    private final EducationRepository educationRepository;
    private final UserProjectRepository userProjectRepository;

    @GetMapping("/resumehistory")
    public String showResumeHistory(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Long userId = userDetails.getUserEntity().getUserId();

        // ✅ 기본 정보
        List<BasicInfo> basicInfoList = basicInfoRepository.findAllByUserId(userId);
        model.addAttribute("basicInfoList", basicInfoList);

        // ✅ 학력 정보
        List<Education> educations = educationRepository.findAllByUserId(userId);
        model.addAttribute("educations", educations);

        // ✅ 완료된 프로젝트 내역 (제출 파일 존재 + visible = true)
        List<UserProjectEntity> completedProjects =
                userProjectRepository.findByUser_UserIdAndStatusAndSubmittedFileNameIsNotNullAndVisibleTrue(userId, "완료");
        model.addAttribute("completedProjects", completedProjects);

        return "resume_history";
    }


    // 테스트용 (추후 principal.getName() 등으로 유저 정보 연동 필요)
    private Long getUserIdFromPrincipal(Principal principal) {
        return 1L;
    }
}
