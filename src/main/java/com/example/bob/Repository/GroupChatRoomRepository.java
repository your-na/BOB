package com.example.bob.Repository;

import com.example.bob.Entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {

    Optional<GroupChatRoom> findByTeamId(Long teamId);

    Optional<GroupChatRoom> findByRoomName(String roomName);

}
