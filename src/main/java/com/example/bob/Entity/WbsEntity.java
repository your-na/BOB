package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wbs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WbsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프로젝트인지 공모전인지 구분 ("project", "contest")
    @Column(length = 20, nullable = false)
    private String type;

    // 대상 ID (projectId 또는 contestId)
    @Column(nullable = false)
    private Long targetId;

    // 카테고리명 (예: 설계, 프론트, 디자인 등)
    @Column(length = 100, nullable = false)
    private String category;

    // 작업명 (예: DB 설계, UI 구현, 포스터 제작 등)
    @Column(length = 255, nullable = false)
    private String task;

    // 월 (1월 ~ n월)
    @Column(nullable = false)
    private int month;

    // 주차 (1주 ~ 5주차)
    @Column(nullable = false)
    private int week;

    // 셀이 체크된 상태인지 (초록색)
    @Column(nullable = false)
    private boolean active;
}
