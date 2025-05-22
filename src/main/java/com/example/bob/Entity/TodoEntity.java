package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String startDate;
    private String endDate;

    private String assignee; // ✅ 담당자 닉네임
    private String workspace; // 예: 개인, 프로젝트A 등

    private boolean completed; // ✅ 체크 여부 저장

    @Column(nullable = false)
    private String type; // 프로젝트 유형 (예: "공모전", "프로젝트", "개인")

    @Column(nullable = true)
    private Long targetId;  // 프로젝트/공모전 구분용 ID (선택사항)

}
