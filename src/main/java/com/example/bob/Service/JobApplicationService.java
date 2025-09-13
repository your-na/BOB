package com.example.bob.Service;

import com.example.bob.DTO.JobApplicationDTO;
import com.example.bob.Entity.JobApplicationEntity;
import com.example.bob.Repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.bob.DTO.ApplicantDTO;
import com.example.bob.Entity.JobApplicationStatus;
import com.example.bob.Entity.ResumeEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.ResumeRepository;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.MyResume;
import com.example.bob.Repository.CoJobPostRepository;
import com.example.bob.Repository.MyResumeRepository;



import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;

    private final ResumeRepository resumeRepository;

    private final NotificationService notificationService;

    private final CoJobPostRepository jobPostRepository;

    private final MyResumeRepository myResumeRepository;



    public List<JobApplicationDTO> getUserJobApplications(Long userId) {
        System.out.println("📥 [SERVICE] 호출된 사용자 ID: " + userId);

        List<JobApplicationEntity> applications = jobApplicationRepository.findByUser_UserId(userId);

        System.out.println("📄 [SERVICE] 지원 내역 수: " + applications.size());

        return applications.stream()
                .filter(app -> app.getJobPost() != null && app.getJobPost().getCompany() != null) // 💡 삭제된 공고 제외
                .map(application -> {
                    JobApplicationDTO dto = new JobApplicationDTO();
                    dto.setAppliedDate(new SimpleDateFormat("yyyy.MM.dd").format(application.getResume().getSubmittedAt()));
                    dto.setJobTitle(application.getJobPost().getTitle());
                    dto.setCompanyIntro(application.getJobPost().getCompany().getCoBio());
                    dto.setStatus(application.getStatus().name()); // Enum to String
                    dto.setJobPostId(application.getJobPost().getId());
                    dto.setApplicationId(application.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    // ✅ 특정 공고에 지원한 지원자 목록 조회 (공고 ID 기준)
    public List<ApplicantDTO> getApplicantsByJobPost(Long jobPostId) {
        return jobApplicationRepository
                .findByJobPost_IdAndStatus(jobPostId, JobApplicationStatus.SUBMITTED).stream()

                // 🔄 DTO로 변환
                .map(app -> {
                    String userName = app.getUser().getUserName(); // 🙋‍♂️ 지원자 이름
                    String appliedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(app.getAppliedAt()); // ⏰ 날짜
                    Long resumeId = app.getResume().getId(); // 📄 이력서 ID

                    return new ApplicantDTO(userName, appliedAt, resumeId);
                })

                // 📤 리스트로 반환
                .collect(Collectors.toList());
    }

    // ✅ 지원자 합격 처리 메서드
    public void acceptApplicant(Long resumeId, Long jobPostId, String message) {
        System.out.println("📥 [SERVICE] acceptApplicant 호출됨");

        // 📄 이력서 조회
        ResumeEntity resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    System.out.println("❌ 이력서 조회 실패 - resumeId: " + resumeId);
                    return new RuntimeException("이력서를 찾을 수 없습니다.");
                });

        // 👤 지원자 정보 가져오기
        UserEntity user = resume.getUser();

        // 📦 지원 내역 조회 (가장 최근 이력서 기반)
        JobApplicationEntity application = jobApplicationRepository
                .findTopByResumeOrderByAppliedAtDesc(resume)
                .orElseThrow(() -> {
                    System.out.println("❌ 지원 내역 조회 실패 - resumeId: " + resumeId);
                    return new RuntimeException("지원 내역이 존재하지 않습니다.");
                });

        // 🔄 상태 변경 → 합격
        application.setStatus(JobApplicationStatus.ACCEPTED);
        application.setAcceptedAt(new Date());
        jobApplicationRepository.save(application);
        System.out.println("✅ 상태 저장 완료: ACCEPTED");

        // 📩 합격 알림 전송
        notificationService.sendHireNotification(user, application.getJobPost().getCompany(), message, application.getJobPost());

        System.out.println("✅ 합격 처리 완료");
    }



    // ❎ 지원자 불합격 처리 메서드
    public void rejectApplicant(Long resumeId, Long jobPostId, String message) {
        System.out.println("📥 [SERVICE] rejectApplicant 호출됨");

        // 📄 이력서 조회
        ResumeEntity resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    System.out.println("❌ 이력서 조회 실패 - resumeId: " + resumeId);
                    return new RuntimeException("이력서를 찾을 수 없습니다.");
                });

        // 👤 지원자 정보 가져오기
        UserEntity user = resume.getUser();

        // 📦 지원 내역 조회 (가장 최근 이력서 기반)
        JobApplicationEntity application = jobApplicationRepository
                .findTopByResumeOrderByAppliedAtDesc(resume)
                .orElseThrow(() -> {
                    System.out.println("❌ 지원 내역 조회 실패 - resumeId: " + resumeId);
                    return new RuntimeException("지원 내역이 존재하지 않습니다.");
                });

        // 🔄 상태 변경 → 불합격
        application.setStatus(JobApplicationStatus.REJECTED);
        jobApplicationRepository.save(application);
        System.out.println("✅ 상태 저장 완료: REJECTED");

        // 📩 불합격 알림 전송
        notificationService.sendRejectNotification(user, application.getJobPost().getCompany(), application.getJobPost());

        // (선택) 알림 기능이 있다면 여기에 삽입 가능
        // notificationService.sendRejectNotification(user, message);

        System.out.println("✅ 불합격 처리 완료");
    }

    // 🙈 지원자 숨기기 처리 메서드
    public void hideApplication(Long applicationId) {
        System.out.println("📥 [SERVICE] hideApplication 호출됨");

        // 지원 내역 조회
        JobApplicationEntity application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> {
                    System.out.println("❌ 지원 내역 조회 실패 - applicationId: " + applicationId);
                    return new RuntimeException("지원 내역을 찾을 수 없습니다.");
                });

        // 상태를 HIDDEN으로 변경
        application.setStatus(JobApplicationStatus.HIDDEN);
        jobApplicationRepository.save(application);

        System.out.println("✅ 상태 저장 완료: HIDDEN");
    }

    // ✅ 이미 조회한 MyResume 객체를 받아서 처리
    public void applyForJobWithMyResume(UserEntity user, Long jobPostId, MyResume myResume) {

        // 1️⃣ 중복 지원 체크
        boolean alreadyApplied = jobApplicationRepository
                .existsByUserAndJobPost_IdAndMyResumeAndStatus(user, jobPostId, myResume, JobApplicationStatus.SUBMITTED);

        if (alreadyApplied) {
            throw new RuntimeException("이미 해당 공고에 이력서를 제출했습니다.");
        }

        // 2️⃣ 공고 조회
        CoJobPostEntity jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고를 찾을 수 없습니다."));

        // 3️⃣ 지원 내역 생성 및 저장
        JobApplicationEntity application = JobApplicationEntity.builder()
                .user(user)
                .jobPost(jobPost)
                .myResume(myResume)
                .appliedAt(new Date())
                .status(JobApplicationStatus.SUBMITTED)
                .build();

        jobApplicationRepository.save(application);

        System.out.println("✅ 내가 만든 이력서 지원 저장 완료 - jobPostId=" + jobPostId + ", myResumeId=" + myResume.getId());
    }






}
