package com.example.bob.Repository;

import com.example.bob.Entity.GroupChatParticipant;
import com.example.bob.Entity.GroupChatRoom;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupChatParticipantRepository extends JpaRepository<GroupChatParticipant, Long> {

    // 특정 채팅방에 참여 중인 모든 사용자
    List<GroupChatParticipant> findByGroupChatRoom(GroupChatRoom room);

    // 특정 유저가 참여 중인 채팅방들
    List<GroupChatParticipant> findByUser(UserEntity user);

    // 특정 유저가 특정 채팅방에 참여 중인지 여부
    Optional<GroupChatParticipant> findByGroupChatRoomAndUser(GroupChatRoom room, UserEntity user);

    // 사용자 ID 기준 조회 (단체 채팅 목록용)
    List<GroupChatParticipant> findByUser_Id(Long userId);

    List<GroupChatParticipant> findByGroupChatRoom_Id(Long roomId);

    @Query("SELECT gp.user FROM GroupChatParticipant gp WHERE gp.groupChatRoom.id = :roomId")
    List<UserEntity> findUsersByRoomId(@Param("roomId") Long roomId);


}
