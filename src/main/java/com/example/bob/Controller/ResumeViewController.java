package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumeViewController {

    // 이력서 작성 페이지 렌더링
    @GetMapping("/resume/write")
    public String showResumeWritePage(@RequestParam("id") Long resumeId,
                                      @RequestParam(value = "jobPostId", required = false) Long jobPostId,
                                      Model model) {
        // Thymeleaf에서 사용할 수 있도록 전달
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("jobPostId", jobPostId);
        return "user_resume2";  // templates/user_resume2.html
    }
}
