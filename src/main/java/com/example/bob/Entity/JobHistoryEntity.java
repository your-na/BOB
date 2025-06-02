package com.example.bob.Entity;

import com.example.bob.DTO.JobHistoryDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 여러 경력이 하나의 사용자에게 연결
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(nullable = false)
    private String status; // 재직 / 퇴직

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 100)
    private String workplace; // 근무지

    @Column(length = 100)
    private String jobTitle; // 직무

    // ✅ DTO → Entity 변환
    public static JobHistoryEntity fromDTO(JobHistoryDTO dto, UserEntity user) {
        return JobHistoryEntity.builder()
                .status(dto.getStatus())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .workplace(dto.getWorkplace())
                .jobTitle(dto.getJobTitle())
                .userEntity(user)
                .build();
    }

    // ✅ Entity → DTO 변환
    public JobHistoryDTO toDTO() {
        return JobHistoryDTO.builder()
                .id(this.id)
                .status(this.status)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .workplace(this.workplace)
                .jobTitle(this.jobTitle)
                .build();
    }
}
