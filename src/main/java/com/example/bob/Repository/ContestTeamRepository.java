package com.example.bob.Repository;

import com.example.bob.Entity.ContestTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestTeamRepository extends JpaRepository<ContestTeamEntity, Long> {
}

