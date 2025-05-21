package com.example.bob.Controller;

import com.example.bob.DTO.*;
import com.example.bob.Service.ResumeService;
import com.example.bob.Entity.UserEntity;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;
import jakarta.servlet.http.HttpSession;



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
            try {
                resumeService.submitUserResume(request, user);
                return ResponseEntity.ok("이력서가 성공적으로 제출되었습니다.");
            } catch (IllegalStateException e) {
                return ResponseEntity.badRequest().body(e.getMessage()); // 중복 지원 예외 메시지
            } catch (Exception e) {
                return ResponseEntity.status(500).body("이력서 제출 중 오류가 발생했습니다.");
            }
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

    // ✅ 특정 공고에 제출한 이력서 상세 조회 API (User + Company 모두 허용)
    @GetMapping("/detail")
    public ResponseEntity<ResumeDetailDTO> getResumeDetail(@RequestParam("jobPostId") Long jobPostId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            // 👤 일반 사용자일 경우
            if (principal instanceof UserDetailsImpl userDetails) {
                UserEntity user = userDetails.getUserEntity();
                ResumeDetailDTO resume = resumeService.getResumeForJobPost(jobPostId, user);
                return ResponseEntity.ok(resume);
            }

            // 🏢 기업 사용자일 경우
            if (principal instanceof com.example.bob.security.CompanyDetailsImpl) {
                ResumeDetailDTO resume = resumeService.getResumeForCompany(jobPostId);
                return ResponseEntity.ok(resume);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    // ❌ 이력서 지원 취소
    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelResume(@RequestParam("jobPostId") Long jobPostId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            UserEntity user = userDetails.getUserEntity();
            boolean canceled = resumeService.cancelJobApplication(jobPostId, user);
            if (canceled) {
                return ResponseEntity.ok("지원이 성공적으로 취소되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("지원 내역이 없거나 이미 취소된 상태입니다.");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자만 취소할 수 있습니다.");
    }

    // ✅ 미리보기
    @PostMapping("/preview")
    public ResponseEntity<String> previewResume(@RequestBody ResumeSubmitRequestDTO request, HttpSession session) {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("이력서 미리보기");

        List<ResumeSectionDTO> sections = new ArrayList<>();
        for (ResumeSectionSubmitDTO section : request.getSections()) {
            ResumeSectionDTO dtoSection = new ResumeSectionDTO();
            dtoSection.setTitle(section.getTitle());
            dtoSection.setType(section.getType());
            dtoSection.setContent(section.getContent());
            dtoSection.setEducations(section.getEducations());
            dtoSection.setSelectedTags(section.getSelectedTags());
            dtoSection.setDragItems(section.getDragItems());


            // ✅ 핵심: uploadedFileName이 있고, 기존 fileNames가 null 또는 비어있으면 대체해줌
            if ((section.getFileNames() == null || section.getFileNames().isEmpty()) &&
                    section.getUploadedFileName() != null && !section.getUploadedFileName().isEmpty()) {
                dtoSection.setFileNames(List.of(section.getUploadedFileName()));
            } else {
                dtoSection.setFileNames(section.getFileNames());
            }

            sections.add(dtoSection);
        }

        dto.setSections(sections);
        session.setAttribute("previewResume", dto);
        return ResponseEntity.ok("미리보기 저장 완료");
    }




}
    