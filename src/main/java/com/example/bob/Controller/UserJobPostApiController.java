package com.example.bob.Controller;

import com.example.bob.Service.UserJobPostService;
import com.example.bob.DTO.UserJobPostDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserJobPostApiController {

    @Autowired
    private UserJobPostService userJobPostService;

    // API: 공고 상세 정보를 반환하는 엔드포인트
    @GetMapping("/api/jobposts/{id}")
    public UserJobPostDetailDTO getJobPostDetail(@PathVariable Long id) {
        return userJobPostService.getJobPostDetail(id);  // 공고 상세 정보를 반환
    }
}
