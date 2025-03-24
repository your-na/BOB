package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "company_history")
public class CompanyHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "companyId", nullable = false)
    private CompanyEntity company; //기업 엔티티 연결

    @Column(length = 100)
    private String coName;

    @Column(length = 100)
    private String coPwd;

    @Column(length = 100)
    private String coEmail;

    @Column(length = 100)
    private String coNick;

    @Column(length = 100)
    private String coPhone;

    @Column(length = 100)
    private String coNum;

    private String coImageUrl;

    @Column(length = 500)
    private String coBio;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public CompanyHistoryEntity(CompanyEntity companyEntity, String coName, String coPwd, String coEmail, String coNick, String coPhone, String coNum, String coImageUrl,String coBio, LocalDateTime updatedAt) {
        this.company = companyEntity;
        this.coName = coName;
        this.coPwd = coPwd;
        this.coEmail = coEmail;
        this.coNick = coNick;
        this.coPhone = coPhone;
        this.coNum = coNum;
        this.coImageUrl = coImageUrl;
        this.coBio = coBio;
        this.updatedAt = updatedAt;
    }
}
