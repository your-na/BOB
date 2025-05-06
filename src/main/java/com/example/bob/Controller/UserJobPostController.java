package com.example.bob.Controller;

import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Service.CoJobPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/job-post")
public class UserJobPostController {

    @Autowired
    private CoJobPostService coJobPostService;

    // ✅ 사용자에게 필요한 공고 정보만 제공 (기업명 + 기간)
    @GetMapping("/info")
    public Map<String, String> getPublicJobPostInfo(@RequestParam("jobPostId") Long jobPostId) {
        CoJobPostEntity jobPost = coJobPostService.getJobPostDetail(jobPostId);

        Map<String, String> result = new HashMap<>();
        result.put("companyName", jobPost.getCompany().getCoName());
        result.put("startDate", jobPost.getStartDate());
        result.put("endDate", jobPost.getEndDate());

        return result;
    }
}
