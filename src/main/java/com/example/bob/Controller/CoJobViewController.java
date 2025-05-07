package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class CoJobViewController {


    @GetMapping("/cojobdetail")
    public String goToCojobDetailPage() {
        return "co_jobdetail"; // templates/cojobdetail.html 또는 static/cojobdetail.html
    }

    // 🔍 공고 상세보기 페이지로 이동 (공고 ID를 URL 파라미터로 받음)
    @GetMapping("/jobdetail")
    public String goToJobDetailPage(@RequestParam Long id, Model model) {
        // 📦 공고 ID를 모델에 담아 Thymeleaf나 JS에서 활용 가능하게 전달
        model.addAttribute("jobPostId", id);

        // 🧭 templates/job_detail.html 페이지로 이동
        return "co_jobdetail";
    }

}
