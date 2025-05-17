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
import com.example.bob.security.CompanyDetailsImpl;

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
    public String showResumeDetail(@RequestParam(value = "jobPostId", required = false) Long jobPostId,
                                   @RequestParam(value = "resumeId", required = false) Long resumeId,
                                   Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            // 👤 사용자: 내 이력서 조회
            if (principal instanceof UserDetailsImpl userDetails && resumeId == null) {
                UserEntity user = userDetails.getUserEntity();
                ResumeDetailDTO resume = resumeService.getResumeForJobPost(jobPostId, user);
                model.addAttribute("resume", resume);
                return "resume_detail";
            }

            // 🏢 기업: resumeId 기준으로 이력서 조회
            if (principal instanceof CompanyDetailsImpl && resumeId != null) {
                ResumeDetailDTO resume = resumeService.getResumeForCompanyWithResumeId(resumeId);
                model.addAttribute("resume", resume);
                model.addAttribute("jobPostId", jobPostId); // ✅ 이 줄 추가
                return "resume_detail";
            }
        }

        // 로그인 안 해도 resumeId로 조회 가능하게 허용
        if (resumeId != null) {
            ResumeDetailDTO resume = resumeService.getResumeForCompanyWithResumeId(resumeId);
            model.addAttribute("resume", resume);
            model.addAttribute("jobPostId", jobPostId);
            return "resume_detail";
        }

        return "redirect:/login";
    }



}
