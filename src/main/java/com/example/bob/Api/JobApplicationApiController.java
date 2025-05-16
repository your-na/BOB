    package com.example.bob.Api;

    import com.example.bob.DTO.JobApplicationDTO;
    import com.example.bob.Entity.UserEntity;
    import com.example.bob.security.UserDetailsImpl;
    import com.example.bob.Service.JobApplicationService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.web.bind.annotation.*;
    import com.example.bob.DTO.ApplicantDTO;



    import java.util.List;

    @RestController
    @RequestMapping("/api/applications")
    @RequiredArgsConstructor
    public class JobApplicationApiController {

        private final JobApplicationService jobApplicationService;

        // ✅ 기존 방식 (유지해도 되고 지워도 됨)
        @GetMapping("/{userId}")
        public List<JobApplicationDTO> getApplications(@PathVariable Long userId) {
            return jobApplicationService.getUserJobApplications(userId);
        }

        // ✅ 로그인한 사용자 기반 조회
        @GetMapping("/me")
        public List<JobApplicationDTO> getMyApplications() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
                UserEntity user = userDetails.getUserEntity();
                Long userId = user.getUserId(); // 🟢 핵심: 로그인된 사용자 ID
                System.out.println("✅ [API] 로그인 사용자 ID: " + userId);
                return jobApplicationService.getUserJobApplications(userId);
            }

            // 로그인 안 된 경우 or 예외 상황
            System.out.println("⚠️ 로그인 사용자 정보가 없습니다.");
            return List.of(); // 빈 리스트 반환
        }

        // ✅ 특정 공고에 지원한 지원자 목록 조회 (기업 전용)
        @GetMapping("/jobpost/{jobPostId}/applicants")
        public List<ApplicantDTO> getApplicantsByJobPost(@PathVariable Long jobPostId) {
            return jobApplicationService.   getApplicantsByJobPost(jobPostId);  // ✔️ 메서드명도 동일
        }



    }
