package com.example.bob.Repository;

import com.example.bob.Entity.ContestEntity;
import com.example.bob.Entity.ContestTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestTeamRepository extends JpaRepository<ContestTeamEntity, Long> {

    List<ContestTeamEntity> findByContest(ContestEntity contest);

    List<ContestTeamEntity> findByCreatedBy(String userNick);

    Optional<ContestTeamEntity> findByTeamName(String teamName);

}
