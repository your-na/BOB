package com.example.bob.Controller;

import com.example.bob.DTO.ResumeDetailDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ResumeService;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumeViewController {

    @Autowired
    private ResumeService resumeService;

    // ✅ 이력서 작성 페이지 렌더링 (user_resume2.html로 이동)
    @GetMapping("/resume/write")
    public String showResumeWritePage(@RequestParam("id") Long resumeId,
                                      @RequestParam(value = "jobPostId", required = false) Long jobPostId,
                                      Model model) {
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("jobPostId", jobPostId);
        return "user_resume2";  // templates/user_resume2.html
    }

    // ✅ 특정 공고에 제출한 이력서 상세 조회 (HTML 렌더링용)
    @GetMapping("/resume/detail")
    public String showResumeDetail(@RequestParam("jobPostId") Long jobPostId, Model model) {
        // 로그인 유저 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            UserEntity user = userDetails.getUserEntity();

            // 서비스에서 이력서 정보 가져오기
            ResumeDetailDTO resume = resumeService.getResumeForJobPost(jobPostId, user);

            // Thymeleaf에 전달
            model.addAttribute("resume", resume);

            return "resume_detail"; // templates/resume_detail.html
        }

        // 로그인 안 되어있으면 로그인 페이지로
        return "redirect:/login";
    }
}
