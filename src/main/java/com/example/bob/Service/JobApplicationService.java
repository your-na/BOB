package com.example.bob.Service;

import com.example.bob.DTO.JobApplicationDTO;
import com.example.bob.Entity.JobApplicationEntity;
import com.example.bob.Repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.bob.DTO.ApplicantDTO;
import com.example.bob.Entity.JobApplicationStatus;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;

    public List<JobApplicationDTO> getUserJobApplications(Long userId) {
        System.out.println("📥 [SERVICE] 호출된 사용자 ID: " + userId);

        List<JobApplicationEntity> applications = jobApplicationRepository.findByUser_UserId(userId);

        System.out.println("📄 [SERVICE] 지원 내역 수: " + applications.size());

        return applications.stream().map(application -> {
            JobApplicationDTO dto = new JobApplicationDTO();
            dto.setAppliedDate(new SimpleDateFormat("yyyy.MM.dd").format(application.getResume().getSubmittedAt()));
            dto.setJobTitle(application.getJobPost().getTitle());
            dto.setCompanyIntro(application.getJobPost().getCompany().getCoBio());
            dto.setStatus(application.getStatus().name()); // Enum to String
            dto.setJobPostId(application.getJobPost().getId());
            return dto;
        }).collect(Collectors.toList());
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



}
