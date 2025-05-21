package com.example.bob.Api;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;

    // 기업 대시보드 정보 조회
    @GetMapping("/company")
    public ResponseEntity<Map<String, Object>> getCompanyDashboard(
            @AuthenticationPrincipal CompanyDetailsImpl companyDetails
    ) {
        if (companyDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        CompanyEntity company = companyDetails.getCompanyEntity();
        Map<String, Object> dashboardInfo = dashboardService.getCompanyDashboardInfo(company.getCompanyId());

        return ResponseEntity.ok(dashboardInfo);
    }
}
