package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoRequestDto {

    private String title;
    private String startDate;
    private String endDate;
    private String assignee;   // ✅ 담당자 닉네임
    private String workspace;  // ✅ 스페이스 (예: 개인, 프로젝트A)
    private String type;       // ✅ 할 일 유형 (예: "공모전", "프로젝트", "개인")
    private Long targetId; // 공모전 팀 ID 또는 프로젝트 ID

}
