        package com.example.bob.DTO;

        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.NoArgsConstructor;

        import java.util.List;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public class CoJobPostRequestDTO {
            private String title;
            private String companyIntro;
            private String email;
            private String phone;
            private String companyLink;
            private String career;
            private String education;
            private String preference;
            private List<String> employmentTypes;
            private String salary;
            private String time;
            private String startDate;
            private String endDate;
            private List<Long> resumeIds;
            private String companyName;
        }
