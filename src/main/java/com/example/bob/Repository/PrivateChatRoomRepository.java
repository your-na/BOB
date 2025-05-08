package com.example.bob.Repository;

import com.example.bob.Entity.PrivateChatRoom;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Long> {
    Optional<PrivateChatRoom> findByUserAAndUserBOrUserBAndUserA(UserEntity a1, UserEntity b1, UserEntity a2, UserEntity b2);

    @Query("SELECT r FROM PrivateChatRoom r " +
            "JOIN FETCH r.userA " +
            "JOIN FETCH r.userB " +
            "WHERE r.id = :roomId")
    Optional<PrivateChatRoom> findByIdWithUsers(@Param("roomId") Long roomId);

}
