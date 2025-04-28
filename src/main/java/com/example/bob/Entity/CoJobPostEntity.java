package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import com.example.bob.Entity.JobStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoJobPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String companyIntro;
    private String email;
    private String phone;
    private String companyLink;
    private String career;
    private String education;
    private String preference;
    private String employmentTypes;
    private String salary;
    private String time;
    private String startDate;
    private String endDate;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({"CompanyHistories"}) // 또는 모든 관련 필드
    private CompanyEntity company;

    @ManyToMany
    @JoinTable(
            name = "job_resume",
            joinColumns = @JoinColumn(name = "job_post_id"),
            inverseJoinColumns = @JoinColumn(name = "resume_id")
    )
    @JsonManagedReference  // 직렬화 시 문제 해결을 위한 추가
    private List<CoResumeEntity> resumes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.WAITING;


    @Override
    public String toString() {
        return "CoJobPostEntity{" +
                "title='" + title + '\'' +
                ", companyIntro='" + companyIntro + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", companyLink='" + companyLink + '\'' +
                ", career='" + career + '\'' +
                ", education='" + education + '\'' +
                ", preference='" + preference + '\'' +
                ", employmentTypes='" + employmentTypes + '\'' +
                ", salary='" + salary + '\'' +
                ", time='" + time + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

