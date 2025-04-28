package com.example.bob.Controller;

import com.example.bob.Service.UserJobPostService;
import com.example.bob.DTO.UserJobPostDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserJobViewController {

    @Autowired
    private UserJobPostService userJobPostService;

    // 사용자 공고 상세보기 페이지
    @GetMapping("/jobindex")
    public String goToJobDetailPage(@RequestParam("id") Long jobId, Model model) {
        // 해당 ID에 맞는 공고 상세 정보를 서비스에서 불러옴
        UserJobPostDetailDTO jobPostDetail = userJobPostService.getJobPostDetail(jobId);

        // model에 공고 상세 정보 전달
        model.addAttribute("jobPost", jobPostDetail);

        return "jobindex"; // 사용자측 상세보기 페이지
    }
}
