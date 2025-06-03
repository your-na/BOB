package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatisticsDTO {
    private long totalUsers;          // 전체 회원 수
    private long generalMemberCount;  // 일반 회원 수
    private long companyMemberCount;  // 기업 회원 수
}
