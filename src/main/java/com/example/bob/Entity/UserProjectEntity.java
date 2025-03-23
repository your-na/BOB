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

    // ✅ 상태 변경 시 프로젝트도 함께 변경
    public void setStatusAndSyncProject(String status) {
        this.status = status;
        if (this.project != null) {
            this.project.completeProject(); // 프로젝트 완료 체크
        }
    }

    // ✅ 팀원이 파일 제출 시 상태 업데이트
    public void submitFile(String fileName) {
        this.submittedFileName = fileName;
        this.submissionDate = LocalDate.now();

        if (this.user.getUserNick().equals(this.project.getCreatedBy())) {
            this.project.completeProject(); // 주최자가 제출하면 프로젝트 완료
        }
    }

    // 역할 (주최/참여)
    public String getRole() {
        return project.getCreatedBy().equals(user.getUserNick()) ? "주최" : "참여";
    }
}
