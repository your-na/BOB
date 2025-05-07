package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📌 이력서 작성 > 나의 경력 및 포트폴리오 > 프로젝트 탭에 보여질 데이터 DTO
 * - 프로젝트 제목과 제출 날짜만 전달함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectResponseDTO {
    private String title;         // 프로젝트 제목
    private String submittedDate; // 제출 날짜 (yyyy-MM-dd 형식)
}
