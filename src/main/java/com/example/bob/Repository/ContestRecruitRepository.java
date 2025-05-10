package com.example.bob.Repository;

import com.example.bob.Entity.ContestRecruitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRecruitRepository extends JpaRepository<ContestRecruitEntity, Long> {
    List<ContestRecruitEntity> findByContest_Id(Long contestId);
}
