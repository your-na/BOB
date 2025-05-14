package com.example.bob.Service;

import com.example.bob.DTO.JobApplicationDTO;
import com.example.bob.Entity.JobApplicationEntity;
import com.example.bob.Repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
