package com.example.bob.Service;

import com.example.bob.Entity.JobApplicationEntity;
import com.example.bob.Repository.CoJobPostRepository;
import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CoJobPostRepository coJobPostRepository;
    private final CoResumeRepository coResumeRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public Map<String, Object> getCompanyDashboardInfo(Long companyId) {
        Map<String, Object> result = new HashMap<>();

        // 📌 공고 수
        int postCount = coJobPostRepository.countByCompany_CompanyId(companyId);
        result.put("postCount", postCount);

        // 📌 지원자 수
        int applicantCount = coJobPostRepository.countApplicantsByCompanyId(companyId);
        result.put("applicantCount", applicantCount);

        // 📌 이력서 양식 수
        int resumeCount = coResumeRepository.countByCompany_CompanyId(companyId);
        result.put("resumeCount", resumeCount);

        // 📌 최근 지원자 (중복 제거)
        List<JobApplicationEntity> recentApplications = jobApplicationRepository
                .findTop3RecentApplicants(companyId, PageRequest.of(0, 20)); // 넉넉하게 불러오기

        List<JobApplicationEntity> uniqueApplicants = new ArrayList<>();
        Set<Long> seenUserIds = new HashSet<>();

        for (JobApplicationEntity app : recentApplications) {
            Long userId = app.getUser().getUserId();
            if (!seenUserIds.contains(userId)) {
                seenUserIds.add(userId);
                uniqueApplicants.add(app);
            }
            if (uniqueApplicants.size() == 3) break; // 최대 3명까지
        }

        result.put("recentApplicants", uniqueApplicants);

        return result;
    }
}
