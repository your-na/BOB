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
    import com.example.bob.DTO.PassRequestDTO;         // ✅ 합격 요청 DTO
    import com.example.bob.Entity.CompanyEntity;       // ✅ 기업 엔티티
    import com.example.bob.Entity.ResumeEntity;        // ✅ 이력서 엔티티
    import com.example.bob.Repository.ResumeRepository; // ✅ 이력서 레포지토리
    import com.example.bob.Service.NotificationService; // ✅ 알림 서비스
    import com.example.bob.security.CompanyDetailsImpl; // ✅ 기업 인증 객체
    import org.springframework.http.ResponseEntity;     // ✅ 응답 처리
    import org.springframework.security.core.annotation.AuthenticationPrincipal; // ✅ 로그인 정보 주입
    import com.example.bob.Entity.CoJobPostEntity;
    import com.example.bob.Entity.CoJobPostEntity;
    import com.example.bob.Repository.CoJobPostRepository;
    import java.util.Map;








    import java.util.List;

    @RestController
    @RequestMapping("/api/applications")
    @RequiredArgsConstructor
    public class JobApplicationApiController {

        private final JobApplicationService jobApplicationService;

        private final ResumeRepository resumeRepository;

        private final NotificationService notificationService;

        private final CoJobPostRepository jobPostRepository;



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

        @PostMapping(value = "/job/pass", produces = "application/json")
        public ResponseEntity<Map<String, String>> passApplicant(
                @AuthenticationPrincipal CompanyDetailsImpl companyDetails,
                @RequestBody PassRequestDTO passRequest) {

            System.out.println("📌 [API 호출] /job/pass");

            // 🔒 로그인 확인
            if (companyDetails == null) {
                System.out.println("❌ companyDetails가 null입니다.");
                return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
            }

            CompanyEntity company = companyDetails.getCompanyEntity();
            System.out.println("✅ [합격 처리] 기업 ID: " + company.getCompanyId());

            // 🧾 이력서 조회
            ResumeEntity resume = resumeRepository.findById(passRequest.getResumeId())
                    .orElseThrow(() -> {
                        System.out.println("❌ 이력서 조회 실패 - resumeId: " + passRequest.getResumeId());
                        return new RuntimeException("이력서를 찾을 수 없습니다.");
                    });
            System.out.println("✅ 이력서 조회 성공 - resumeId: " + resume.getId());

            UserEntity applicant = resume.getUser();
            System.out.println("👤 지원자: " + applicant.getUserNick() + " (ID: " + applicant.getUserId() + ")");

            // 💼 공고 조회
            CoJobPostEntity jobPost = jobPostRepository.findById(passRequest.getJobPostId())
                    .orElseThrow(() -> {
                        System.out.println("❌ 공고 조회 실패 - jobPostId: " + passRequest.getJobPostId());
                        return new RuntimeException("공고를 찾을 수 없습니다.");
                    });
            System.out.println("✅ 공고 조회 성공 - jobPostId: " + jobPost.getId());

            // 📩 알림 전송
            System.out.println("📨 합격 알림 전송 시작...");
            try {
                notificationService.sendHireNotification(applicant, company, passRequest.getMessage(), jobPost);
                jobApplicationService.acceptApplicant(resume.getId(), jobPost.getId(), passRequest.getMessage());
                System.out.println("✅ 알림 전송 완료");
            } catch (Exception e) {
                System.out.println("❌ 알림 전송 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("message", "알림 전송 실패"));
            }

            // ✅ 이력서 상태 변경
            resume.setPassed(true);
            resumeRepository.save(resume);
            System.out.println("✅ 이력서 상태 저장 완료");

            // 🎉 응답 반환
            return ResponseEntity.ok(Map.of("message", "🎉 합격 알림이 전송되었습니다."));
        }

        // ❎ 지원자 불합격 처리 API
        @PostMapping(value = "/job/reject", produces = "application/json")
        public ResponseEntity<Map<String, String>> rejectApplicant(
                @AuthenticationPrincipal CompanyDetailsImpl companyDetails,
                @RequestBody PassRequestDTO rejectRequest) {

            System.out.println("📌 [API 호출] /job/reject");

            // 🔒 기업 로그인 여부 확인
            if (companyDetails == null) {
                System.out.println("❌ 기업 로그인 필요");
                return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
            }

            try {
                // 🛠 불합격 처리 로직 실행
                jobApplicationService.rejectApplicant(
                        rejectRequest.getResumeId(),
                        rejectRequest.getJobPostId(),
                        rejectRequest.getMessage()
                );

                // ✅ 성공 응답 반환
                return ResponseEntity.ok(Map.of("message", "❎ 불합격 처리가 완료되었습니다."));
            } catch (Exception e) {
                // ⚠️ 예외 발생 시 로그 출력 + 실패 응답
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("message", "서버 오류: " + e.getMessage()));
            }
        }







    }
