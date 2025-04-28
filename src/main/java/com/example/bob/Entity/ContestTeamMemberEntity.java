package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "contest_team_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestTeamMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private ContestTeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private boolean isAccepted; // 초대 수락 여부

    @Column(nullable = false)
    private boolean isInvitePending; // 초대 상태인지 여부


    @Column(nullable = false)
    private String role; // "LEADER" 또는 "MEMBER"

    @Column
    private LocalDate joinedAt;

    @PrePersist
    public void setJoinedAt() {
        this.joinedAt = LocalDate.now();
    }
}

