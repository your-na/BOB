package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📌 이력서 작성 > 나의 경력 및 포트폴리오 > 프로젝트 탭에 보여질 데이터 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectResponseDTO {
    private Long id;              // ✅ 프로젝트 ID (제일 위에 선언!)
    private String title;         // 프로젝트 제목
    private String submittedDate; // 제출 날짜 (yyyy-MM-dd 형식)
    private String startDate;     // yyyy-MM-dd
    private String endDate;       // yyyy-MM-dd
    private String submittedFileName; // ex) file_123.jpg
    private String filePath;          // ex) /download/file_123.jpg
}
