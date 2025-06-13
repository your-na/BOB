package com.example.bob.Controller;

import com.example.bob.Entity.BasicInfo;
import com.example.bob.Entity.Education;
import com.example.bob.Repository.BasicInfoRepository;
import com.example.bob.Repository.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class ResumeHistoryController {

    private final BasicInfoRepository basicInfoRepository;
    private final EducationRepository educationRepository;

    // ✅ resumehistory 페이지 진입 시 기본정보 조회해서 넘겨줌
    @GetMapping("/resumehistory")
    public String showResumeHistory(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);

        List<BasicInfo> basicInfoList = basicInfoRepository.findAllByUserId(userId);
        model.addAttribute("basicInfoList", basicInfoList);

        // 🔽 학력 리스트 추가
        List<Education> educations = educationRepository.findAllByUserId(userId);
        model.addAttribute("educations", educations);

        return "resume_history";
    }


    // ✅ 테스트용 로그인 유저 ID (나중에 연동 시 교체)
    private Long getUserIdFromPrincipal(Principal principal) {
        return 1L;
    }
}
