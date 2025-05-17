package com.example.bob.Repository;

import com.example.bob.Entity.ContestEntity;
import com.example.bob.Entity.ContestTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestTeamRepository extends JpaRepository<ContestTeamEntity, Long> {

    List<ContestTeamEntity> findByContest(ContestEntity contest);

}
