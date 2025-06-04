package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMemberCountDTO {
    private long activeGeneralMembers;  // 최근 30일 활동한 일반 회원 수
    private long activeCompanyMembers;  // 최근 30일 활동한 기업 회원 수
    private long totalGeneralMembers;  // 토탈
    private long totalCompanyMembers;  //
}
