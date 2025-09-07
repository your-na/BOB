package com.example.bob.DTO;

import lombok.*;
import java.util.List;

/**
 * 이력서의 개별 섹션 DTO
 * - 타입, 제목, 설명, 본문, 태그, 조건 포함
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResumeSectionDto {

    private String type; // 서술형, 선택형, 첨부 등

    private String title; // 섹션 제목

    private String comment; // 설명 문구

    private String content; // 사용자 입력 내용

    private boolean multiSelect; // 복수 선택 여부

    private List<String> tags; // 사용자 입력 태그

    private List<String> conditions; // 선택한 조건

    // ✅ [추가] 드래그 항목 목록
    private List<MyResumeDragItemDto> dragItems;
}
