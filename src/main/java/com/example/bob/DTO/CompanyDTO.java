package com.example.bob.DTO;

import com.example.bob.Entity.CompanyEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class CompanyDTO {

    private Long companyId;
    private String coName; // 기업명
    private String coIdLogin;
    private String coPwd;
    private String coEmail;
    private String coNick; // 담당자명
    private String coPhone;
    private String coNum;
    private String coBio;
    private String coImageUrl;
    private LocalDateTime createdAt;

    public static CompanyDTO toCompanyDTO(CompanyEntity companyEntity) {
        CompanyDTO companyDTO = new CompanyDTO();

        companyDTO.setCompanyId(companyEntity.getCompanyId());
        companyDTO.setCoName(companyEntity.getCoName());
        companyDTO.setCoIdLogin(companyEntity.getCoIdLogin());
        companyDTO.setCoPwd(companyEntity.getPwd());
        companyDTO.setCoEmail(companyEntity.getCoEmail());
        companyDTO.setCoNick(companyEntity.getCoNick());
        companyDTO.setCoPhone(companyEntity.getCoPhone());
        companyDTO.setCoNum(companyEntity.getCoNum());
        companyDTO.setCoBio(companyEntity.getCoBio());
        companyDTO.setCoImageUrl(companyEntity.getCoImageUrl());
        companyDTO.setCreatedAt(companyEntity.getCreatedAt());

        return companyDTO;
    }

    public CompanyEntity toCompanyEntity() {
        return CompanyEntity.toCompanyEntity(this);
    }
}
