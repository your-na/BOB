package com.example.bob.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContestRecruitDTO {

    private Long contestId;             // 공모전 ID (숨겨진 input)

    private LocalDate projectStartDate; // 진행 시작일 (name="projectStartDate")
    private LocalDate projectEndDate;   // 진행 종료일 (name="projectEndDate")

    private LocalDate recruitStartDate; // 모집 시작일 (name="recruitStartDate")
    private LocalDate recruitEndDate;   // 모집 종료일 (name="recruitEndDate")

    private String recruitCount;           // 모집 인원 (name="recruitCount")
    private String recruitmentCount;        //직접 입력

    public int getRecruitCountAsInt() {
        try {
            // "plus"가 선택된 경우에는 직접 입력값 사용
            if ("plus".equals(recruitCount)) {
                return Integer.parseInt(recruitmentCount);
            }
            // 일반 숫자 선택 시
            return Integer.parseInt(recruitCount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("모집 인원은 숫자여야 합니다.");
        }
    }

    private String title;               // 공모전명 (name="title")
    private String content;             // 상세 설명 (name="content")
}
