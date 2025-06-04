package com.example.bob.Api;

import com.example.bob.DTO.AdminStatisticsDTO;
import com.example.bob.Service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.bob.DTO.ActiveMemberCountDTO;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsApiController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping
    public ResponseEntity<AdminStatisticsDTO> getAdminStatistics() {
        AdminStatisticsDTO stats = adminStatisticsService.getAdminStatistics();
        return ResponseEntity.ok(stats);
    }

    // 최근 1년간 구직 성공률을 반환하는 API 메서드
    @GetMapping("/job-success-rate")
    public ResponseEntity<Double> getJobSuccessRate() {
        double successRate = adminStatisticsService.getJobSuccessRateLastYear();
        return ResponseEntity.ok(successRate);
    }

    // 최근 30일 활동 회원 수 반환 API
    @GetMapping("/active-members")
    public ResponseEntity<ActiveMemberCountDTO> getActiveMembers() {
        ActiveMemberCountDTO activeMembers = adminStatisticsService.getActiveMemberCountLast30Days();
        return ResponseEntity.ok(activeMembers);
    }


}
