package com.example.bob.DTO;

import java.util.List;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CoResumeRequestDTO {

    private String title;
    private List<CoResumeSectionRequestDTO> sections;
    private Date createdAt;
    // Lombok이 @Getter, @Setter, @AllArgsConstructor로 기본적으로 처리


}
