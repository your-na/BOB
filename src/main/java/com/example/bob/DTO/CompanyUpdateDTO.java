    package com.example.bob.DTO;

    import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public class CompanyUpdateDTO {

        // 수정 가능 항목들
        private String coName;        // 기업명 (예: 삼성전자)
        private String coNick;        // 담당자명 (예: 홍길동)
        private String coEmail;       // 이메일 (예: hello@company.com)
        private String coPhone;       // 전화번호 (예: 010-1234-5678)
        private String coBio;         // 기업 소개글
        private String coImageUrl;    // 프로필 이미지 URL

    }
