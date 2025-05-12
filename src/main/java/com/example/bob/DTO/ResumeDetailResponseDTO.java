package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDetailResponseDTO {
    private Long resumeId;
    private String userName;
    private String phone;
    private String email;
    private String birthDate;
    private String gender;
    private String address;
    private Date submittedAt;

    private List<ResumeSectionDetailDTO> sections;
}
