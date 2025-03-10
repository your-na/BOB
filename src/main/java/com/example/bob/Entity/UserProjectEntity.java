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

    private String status; // 사용자의 참여 상태 (참여중, 완료 등)

    // 역할 (프로젝트 생성자라면 주최, 그렇지 않으면 참여)
    public String getRole() {
        return project.getCreatedBy().equals(user.getUserNick()) ? "주최" : "참여";
    }
}
