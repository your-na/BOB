package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "basic_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String birthDate;

    private String region;

    private String email;

    private String phone;

    private Long userId; // 로그인한 사용자 ID 저장용
}
