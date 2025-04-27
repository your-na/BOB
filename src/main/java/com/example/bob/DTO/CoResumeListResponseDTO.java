package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이력서 목록 조회 응답 DTO
 * 프론트에 이력서 제목, 작성일, ID만 내려주기 위한 용도
 */
@Getter
@AllArgsConstructor
public class CoResumeListResponseDTO {

    private Long id;           // 이력서 ID (삭제/수정용)
    private String title;      // 이력서 제목
    private String createdAt;  // 작성일 (yyyy-MM-dd)


    // Lombok이 @Getter, @AllArgsConstructor로 기본적으로 처리
}
