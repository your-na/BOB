package com.example.bob.Repository;

import com.example.bob.Entity.PrivateChatMessage;
import com.example.bob.Entity.PrivateChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivateChatMessageRepository extends JpaRepository<PrivateChatMessage, Long> {
    PrivateChatMessage findTopByRoomIdOrderBySentAtDesc(Long roomId);

    @Query("SELECT r FROM PrivateChatRoom r WHERE r.userA.userId = :userId OR r.userB.userId = :userId")
    List<PrivateChatRoom> findAllByUserId(@Param("userId") Long userId);

}
