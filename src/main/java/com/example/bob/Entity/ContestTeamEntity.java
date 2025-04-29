package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contest_team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestTeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private ContestEntity contest;

    @Column(nullable = false)
    private String createdBy; // 팀장 닉네임

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContestTeamMemberEntity> members = new ArrayList<>();

    @Column(length = 50)
    private String status; // 진행 상태 (예: 모집중, 진행중, 완료 등)

    @Column
    private LocalDate createdAt;

    @PrePersist
    public void setCreateDate() {
        this.createdAt = LocalDate.now();
    }
}

