package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoJobPostResponseDTO {
    private Long id;
    private String title;
    private String phone;
    private String career;
    private String companyNick; // coNick만 뽑아 전송
}
