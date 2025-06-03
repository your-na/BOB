package com.example.bob.Api;

import com.example.bob.DTO.AdminStatisticsDTO;
import com.example.bob.Service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
