package com.example.bob.DTO;

import lombok.*;
import java.util.List;

/**
 * 나만의 이력서 전체 DTO
 * - 제목, 사용자 ID, 섹션 목록 포함
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResumeDto {

    private String title; // 이력서 제목

    private Long memberId; // 사용자 ID

    private List<MyResumeSectionDto> sections; // 섹션 리스트
}
