package com.example.bob.Repository;

import com.example.bob.Entity.MessageReadStatus;
import com.example.bob.Entity.PrivateChatMessage;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {
    List<MessageReadStatus> findByMessage(PrivateChatMessage message);

    Optional<MessageReadStatus> findByMessageAndUser(PrivateChatMessage message, UserEntity user);

    List<MessageReadStatus> findByUserAndIsReadFalse(UserEntity user);

    long countByUser_IdAndIsReadFalseAndMessage_RoomId(Long userId, Long roomId);

}
