package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSectionDetailDTO {
    private String title;                // 섹션명 (예: 자기소개)
    private String type;                 // TEXT / TAG / DRAG / EDUCATION / FILE 등

    private String content;              // 텍스트형 섹션 내용
    private List<String> selectedTags;   // 선택형 태그
    private List<EducationDTO> educations; // 학력형 데이터
    private List<String> dragItems;      // 드래그형 항목
    private List<String> fileNames;      // 파일명 리스트 (첨부파일/이미지)
}
