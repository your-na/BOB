package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String schoolName;

    private String status;     // 재학 or 졸업

    private String startDate;  // yyyy-MM-dd
    private String endDate;    // yyyy-MM-dd
}
