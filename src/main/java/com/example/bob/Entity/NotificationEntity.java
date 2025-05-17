package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.NotificationType;




@Entity
@Getter
@Setter
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;  // 알림 내용
    private boolean isRead;  // 읽음 여부

    @ManyToOne
    @JoinColumn(name = "user_id")  // 알림을 받는 사용자
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company; // 기업 사용자의 알림

    private LocalDateTime timestamp;  // 알림 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;  // 알림 유형


    @ManyToOne
    @JoinColumn(name = "contest_team_id")
    private ContestTeamEntity contestTeam; // 공모전 팀 관련 알림


    @ManyToOne
    @JoinColumn(name = "sender_id")  // 신청을 보낸 사람
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "project_id")  // 관련된 프로젝트
    private ProjectEntity project;

    public NotificationEntity() {}

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isRead() {
        return isRead;
    }

    // 🔹 알림 클릭 시 이동할 링크 자동 생성
    public String getLink() {
        if (project != null && sender != null && sender.getUserId() != null) {
            return "/teamrequest/" + project.getId() + "/" + sender.getUserId();
        } else if (contestTeam != null) {
            return "/contest/team/invite?teamId=" + contestTeam.getId();
        }
        return "#";
    }


}
