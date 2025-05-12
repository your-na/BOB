package com.example.bob.Repository;

import com.example.bob.Entity.GroupChatMessage;
import com.example.bob.Entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {

    // 특정 채팅방의 모든 메시지 (오래된 순)
    List<GroupChatMessage> findByGroupChatRoomOrderBySentAtAsc(GroupChatRoom room);

    // 최신 메시지 하나
    GroupChatMessage findTopByGroupChatRoomOrderBySentAtDesc(GroupChatRoom room);
}
