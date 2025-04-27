package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "job_resume",
            joinColumns = @JoinColumn(name = "job_post_id"),
            inverseJoinColumns = @JoinColumn(name = "resume_id")
    )
    private List<CoResumeEntity> resumes = new ArrayList<>();
}
