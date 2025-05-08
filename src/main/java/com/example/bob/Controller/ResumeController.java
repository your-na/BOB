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
import com.example.bob.DTO.ResumeSubmitRequestDTO;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;



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

    // ✅ 사용자가 작성한 이력서를 제출하는 API
    @PostMapping("/submit")
    public ResponseEntity<String> submitResume(@RequestBody ResumeSubmitRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            UserEntity user = userDetails.getUserEntity();
            resumeService.submitUserResume(request, user);
            return ResponseEntity.ok("이력서가 성공적으로 제출되었습니다.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자만 제출할 수 있습니다.");
    }

    // ✅ 사용자가 넣은 파일
    @PostMapping("/upload")  // 🔥 경로는 /api/user/resumes/upload
    public ResponseEntity<String> uploadResumeFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "C:/uploads/resume/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String uniqueName = UUID.randomUUID() + extension;

            File dest = new File(dir, uniqueName);
            file.transferTo(dest);

            return ResponseEntity.ok(uniqueName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("파일 업로드 실패: " + e.getMessage());
        }
    }






}
    