package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResumeSectionSubmitDTO {

    private Long coSectionId;           // 어떤 기업 섹션(CoResumeSectionEntity) 기반인지
    private String content;             // 사용자가 입력한 텍스트 (서술형, 자기소개 등)
    private List<String> selectedTags;  // 사용자가 선택한 태그들 (선택형일 경우)
    private String uploadedFileName;    // 파일 또는 사진 첨부 섹션일 경우 업로드된 파일명.
    private List<String> fileNames; // ✅ 여러 개의 파일 저장용
    private List<ResumeDragItemDTO> dragItems;

    /**
     * 학력사항 섹션일 경우에만 채워지는 필드.
     * 하나의 섹션 내에 여러 개의 학력 항목을 포함할 수 있음.
     */
    private List<EducationDTO> educations;

    private String title;  // 섹션 제목 (예: 자기소개, 희망직무 등)
    private String type;   // 섹션 유형 (예: 서술형, 선택형 등)






}
