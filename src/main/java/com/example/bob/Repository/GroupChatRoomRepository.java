package com.example.bob.Repository;

import com.example.bob.Entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    // 추가적인 쿼리가 필요하면 여기에 작성
}
