    package com.example.bob.DTO;

    import lombok.AllArgsConstructor;
    import lombok.Data;
    import com.example.bob.Entity.JobStatus;


    @Data
    @AllArgsConstructor
    public class CoJobPostResponseDTO {
        private Long id;
        private String title;
        private String phone;
        private String career;
        private String companyNick; // coNick만 뽑아 전송
        private String startDate;
        private String endDate;
        private JobStatus status;
    }
