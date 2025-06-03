package com.example.bob.Service;

import com.example.bob.DTO.AdminStatisticsDTO;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Repository.CompanyRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

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
}
