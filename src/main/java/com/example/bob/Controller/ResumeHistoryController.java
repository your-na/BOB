package com.example.bob.Controller;

import com.example.bob.Entity.BasicInfo;
import com.example.bob.Repository.BasicInfoRepository;
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

    // ✅ resumehistory 페이지 진입 시 기본정보 조회해서 넘겨줌
    @GetMapping("/resumehistory")
    public String showResumeHistory(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal); // 로그인 ID 추출
        List<BasicInfo> basicInfoList = basicInfoRepository.findAllByUserId(userId);
        model.addAttribute("basicInfoList", basicInfoList); // 뷰로 전달
        return "resume_history"; // 템플릿 이름
    }

    // ✅ 테스트용 로그인 유저 ID (나중에 연동 시 교체)
    private Long getUserIdFromPrincipal(Principal principal) {
        return 1L;
    }
}
