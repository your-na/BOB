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
    private List<String> tags;         // 선택형일 때 사용
    private boolean multiSelect;       // 복수선택 여부
    private List<String> conditions;   // 조건 항목들 (예: 50자 이상, 200자 이상 등)
    private String directInputValue;   // 직접입력 값 (사용자가 입력한 값)

    // 기본 생성자, Lombok을 사용하여 @Getter, @Setter, @AllArgsConstructor 처리됨

    // 추가된 안전장치: conditions 필드가 null일 경우 빈 리스트로 초기화
    public List<String> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<>();  // conditions가 null이면 빈 리스트로 초기화
        }
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }
}
