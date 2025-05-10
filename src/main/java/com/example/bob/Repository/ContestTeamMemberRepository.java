package com.example.bob.Repository;

import com.example.bob.Entity.ContestTeamMemberEntity;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestTeamMemberRepository extends JpaRepository<ContestTeamMemberEntity, Long> {
    Optional<ContestTeamMemberEntity> findByTeamIdAndUserId(Long teamId, Long userId);
    List<ContestTeamMemberEntity> findByUserAndIsAcceptedTrue(UserEntity user);

}
