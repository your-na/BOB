package com.example.bob.Repository;

import com.example.bob.Entity.ContestEntity;
import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.ContestTeamMemberEntity;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestTeamMemberRepository extends JpaRepository<ContestTeamMemberEntity, Long> {
    Optional<ContestTeamMemberEntity> findByTeamIdAndUserId(Long teamId, Long userId);

    List<ContestTeamMemberEntity> findByUserAndIsAcceptedTrue(UserEntity user);

    @Query("SELECT m.id FROM ContestTeamMemberEntity m WHERE m.team.id = :teamId AND m.user.userId = :userId AND m.isInvitePending = true")

    Optional<Long> findInviteIdByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    Optional<ContestTeamMemberEntity> findByTeamAndUser(ContestTeamEntity team, UserEntity user);



}
