package com.example.bob.Controller;

import com.example.bob.Service.ResumeService;
import com.example.bob.DTO.ResumeDTO;
import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    // 기업 양식을 기반으로 사용자 이력서 구조를 반환
    @GetMapping("/init")
    public ResumeDTO initResume(@RequestParam("id") Long coResumeId) {
        return resumeService.generateUserResumeFromCo(coResumeId);
    }

    // ✅ 로그인한 사용자 정보만 반환하는 API
    @GetMapping("/me")
    public UserDTO getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            UserEntity user = userDetails.getUserEntity();
            return UserDTO.fromEntity(user);
        }
        return null;
    }
}
    