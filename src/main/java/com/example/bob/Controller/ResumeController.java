package com.example.bob.Controller;

import com.example.bob.Service.ResumeService;
import com.example.bob.DTO.ResumeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/resumes") // 사용자 이력서 API
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    // 기업 양식을 기반으로 사용자 이력서 구조를 반환
    @GetMapping("/init")
    public ResumeDTO initResume(@RequestParam("id") Long coResumeId) {
        return resumeService.generateUserResumeFromCo(coResumeId);
    }
}
