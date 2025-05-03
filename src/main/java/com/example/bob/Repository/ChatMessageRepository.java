package com.example.bob.Repository;

import com.example.bob.Entity.PrivateChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<PrivateChatMessage, Long> {
    List<PrivateChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
}
