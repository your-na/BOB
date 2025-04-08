package com.example.bob.DTO;

/**
 * 이력서 목록 조회 응답 DTO
 * 프론트에 이력서 제목, 작성일, ID만 내려주기 위한 용도
 */
public class CoResumeListResponseDTO {

    private Long id;           // 이력서 ID (삭제/수정용)
    private String title;      // 이력서 제목
    private String createdAt;  // 작성일 (yyyy-MM-dd)

    // 생성자
    public CoResumeListResponseDTO(Long id, String title, String createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    // Getter
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
