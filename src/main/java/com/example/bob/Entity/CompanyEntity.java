package com.example.bob.Entity;

import com.example.bob.DTO.CompanyDTO;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "company")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(length = 100, unique = true)
    private String coNick; // 기업명

    @Column(length = 100, unique = true)
    private String coIdLogin;

    @Column(length = 100)
    private String pwd;

    @Column(length = 100)
    private String coEmail; // 필수 아님

    @Column(length = 100)
    private String coName;  // 담당자 이름

    @Column(length = 100)
    private String coPhone; // 담당자 전화번호

    @Column(length = 100)
    private String coNum;   // 사업자등록번호

    // 기업 로고는 내정보 페이지에서 수정
    @Column(length = 255)
    private String coImageUrl = "/images/user.png"; // 기본 이미지 경로

    // 회원가입 일자
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;


    @Column(length = 500)
    private String coBio = "소개를 작성해보세요.";

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyHistoryEntity> CompanyHistories = new ArrayList<>();

    public static CompanyEntity toCompanyEntity(CompanyDTO companyDTO) {
        CompanyEntity companyEntity = new CompanyEntity();

        companyEntity.companyId = companyDTO.getCompanyId();
        companyEntity.coName = companyDTO.getCoName(); // 담당자명
        companyEntity.coIdLogin = companyDTO.getCoIdLogin();
        companyEntity.pwd = companyDTO.getCoPwd();
        companyEntity.coEmail = companyDTO.getCoEmail();
        companyEntity.coNick = companyDTO.getCoNick(); // 기업명
        companyEntity.coPhone = companyDTO.getCoPhone();
        companyEntity.coNum = companyDTO.getCoNum(); // 사업자 등록 번호
        companyEntity.coImageUrl =  (companyDTO.getCoImageUrl() != null) ? companyDTO.getCoImageUrl() : "/images/user.png";
        companyEntity.createdAt = (companyDTO.getCreatedAt() != null) ? companyDTO.getCreatedAt() : LocalDateTime.now();

        return companyEntity;
    }

    public void setCoImageUrl(String coImageUrl) {
        this.coImageUrl = coImageUrl;
    }

    public void addHistory(CompanyHistoryEntity companyHistoryEntity) { this.CompanyHistories.add(companyHistoryEntity);}

    public Long getCompanyId() { return this.companyId; }
}
