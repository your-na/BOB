package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_project")
public class UserProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 관계 ID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 사용자

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project; // 프로젝트

    private LocalDate joinDate; // 사용자가 프로젝트에 참여한 날짜

    private LocalDate submissionDate; // 🔥 제출 날짜

    private String submittedFileName; // 🔥 제출된 파일 이름

    private String status; // 사용자의 참여 상태 (모집중, 신청중, 진행중, 완료)

    private Long teamMemberId; // 🔥 팀원 아이디 추가


    // 역할 (주최/참여)
    public String getRole() {
        return project.getCreatedBy().equals(user.getUserNick()) ? "주최" : "참여";
    }

    // 팀원 아이디를 가져오는 메서드
    public Long getTeamMemberId() {
        return this.user != null ? this.user.getUserId() : null; // user가 존재하면 teamMemberId를 가져옴
    }
}
