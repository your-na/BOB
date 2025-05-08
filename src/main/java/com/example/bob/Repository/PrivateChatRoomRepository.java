package com.example.bob.Repository;

import com.example.bob.Entity.PrivateChatRoom;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Long> {
    Optional<PrivateChatRoom> findByUserAAndUserBOrUserBAndUserA(UserEntity a1, UserEntity b1, UserEntity a2, UserEntity b2);

    @Query("SELECT r FROM PrivateChatRoom r " +
            "JOIN FETCH r.userA " +
            "JOIN FETCH r.userB " +
            "WHERE r.id = :roomId")
    Optional<PrivateChatRoom> findByIdWithUsers(@Param("roomId") Long roomId);

    @Query("SELECT r FROM PrivateChatRoom r WHERE r.userA.userId = :userId OR r.userB.userId = :userId")
    List<PrivateChatRoom> findAllByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT r FROM PrivateChatRoom r
    WHERE r.userA.userId = :userId OR r.userB.userId = :userId
    ORDER BY
        CASE
            WHEN r.userA.userId = :userId AND r.pinnedByA = true THEN 0
            WHEN r.userB.userId = :userId AND r.pinnedByB = true THEN 0
            ELSE 1
        END,
        r.id DESC
""")
    List<PrivateChatRoom> findAllByUserIdSorted(@Param("userId") Long userId);

}
