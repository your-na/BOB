package com.example.bob.Repository;

import com.example.bob.Entity.ContestTeamMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestTeamMemberRepository extends JpaRepository<ContestTeamMemberEntity, Long> {
}
