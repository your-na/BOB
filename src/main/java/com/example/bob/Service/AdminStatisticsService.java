package com.example.bob.Service;

import com.example.bob.DTO.AdminStatisticsDTO;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Repository.CompanyRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.bob.Repository.JobApplicationRepository;



import java.util.Calendar;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;


    // 관리자 대시보드 통계 데이터 조회
    public AdminStatisticsDTO getAdminStatistics() {
        long totalUsers = userRepository.count();               // 전체 회원 수
        long generalUsers = userRepository.countByRole("USER");  // 일반 회원 수
        long companyUsers = companyRepository.count();           // 기업 회원 수

        return AdminStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .generalMemberCount(generalUsers)
                .companyMemberCount(companyUsers)
                .build();

    }

    // 최근 1년 구직 성공률 계산 (합격자 수 / 지원자 수 * 100)
    public double getJobSuccessRateLastYear() {
        // 현재 날짜 기준 1년 전 날짜 계산
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        Date oneYearAgo = cal.getTime();

        // 최근 1년간 지원자 수 (중복 제거)
        long applicants = jobApplicationRepository.countDistinctApplicantsSince(oneYearAgo);

        if (applicants == 0) {
            return 0.0; // 지원자가 없으면 성공률 0%
        }

        // 최근 1년간 합격자 수 (중복 제거)
        long accepted = jobApplicationRepository.countDistinctAcceptedSince(oneYearAgo);

        // 성공률 계산 (소수점 2자리)
        double rate = ((double) accepted / applicants) * 100;
        return Math.round(rate * 100) / 100.0;
    }
}
