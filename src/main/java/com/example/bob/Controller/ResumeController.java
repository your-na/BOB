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
import com.example.bob.DTO.UserProjectResponseDTO;

import java.util.ArrayList;
import java.util.List;


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

    // ✅ 사용자가 제출한 완료된 프로젝트 리스트 반환 (제출된 파일 포함)
    @GetMapping("/projects")
    public List<UserProjectResponseDTO> getMyCompletedProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            UserEntity user = userDetails.getUserEntity();
            return resumeService.getCompletedProjectsForResume(user);
        }
        return new ArrayList<>();
    }


}
    