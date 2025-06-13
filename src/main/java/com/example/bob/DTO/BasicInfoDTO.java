package com.example.bob.DTO;

import lombok.*;

// ✅ 프론트에서 전달되는 기본 정보 데이터를 담는 DTO 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfoDTO {

    private String name;        // 이름
    private String birthDate;   // 생년월일
    private String region;      // 지역
    private String email;       // 이메일
    private String phone;       // 전화번호
}
