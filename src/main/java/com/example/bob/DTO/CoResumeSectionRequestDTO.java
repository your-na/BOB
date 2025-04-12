package com.example.bob.DTO;

import java.util.List;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CoResumeSectionRequestDTO {

    private String type;               // 섹션 유형 (선택형, 서술형 등)
    private String title;              // 섹션 제목
    private String comment;            // 설명/가이드
    private String content;            // 서술형일 때 사용
    private List<String> tags;         // ✅ 선택형 보기 태그
    private boolean multiSelect;       // 복수선택 여부
    private List<String> conditions;   // 조건 항목들 (예: 50자 이상, 200자 이상 등)
    private String directInputValue;   // 직접입력 값 (사용자가 입력한 값)

    // ✅ tags가 null일 경우 빈 리스트 반환
    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // ✅ conditions가 null일 경우 빈 리스트 반환
    public List<String> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }
}
