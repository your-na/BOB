package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserJobPostDetailDTO {
    private String title;
    private String companyIntro;
    private String email;
    private String phone;
    private String career;
    private String education;
    private String employmentTypes;
    private String salary;
    private String time;
    private String preference;
    private String startDate;
    private String endDate;

    // 이력서 양식 제목 리스트
    private List<String> resumeTitles; // 이 부분이 추가되어야 합니다.
}

