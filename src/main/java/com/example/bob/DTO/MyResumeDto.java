package com.example.bob.DTO;

import lombok.*;
import java.util.List;

/**
 * 나만의 이력서 전체 DTO
 * - 제목, 사용자 ID(userIdLogin), 섹션 목록 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyResumeDto {

    private Long id;

    private String title; // 이력서 제목

    private String memberId; // 사용자 ID (userIdLogin)

    private List<MyResumeSectionDto> sections; // 섹션 리스트

    private String userName;
    private String profileImageUrl;
    private String mainLanguage;
    private String sex;
    private String birthday;
    private Integer age;
    private String userPhone;
    private String userEmail;
    private String region;
}
