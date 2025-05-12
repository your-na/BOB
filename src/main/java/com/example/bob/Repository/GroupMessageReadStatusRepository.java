package com.example.bob.Repository;

import com.example.bob.Entity.GroupChatMessage;
import com.example.bob.Entity.GroupMessageReadStatus;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMessageReadStatusRepository extends JpaRepository<GroupMessageReadStatus, Long> {

    // 메시지 기준 전체 읽음 상태 조회
    List<GroupMessageReadStatus> findByMessage(GroupChatMessage message);

    // 유저가 특정 메시지를 읽었는지 조회
    Optional<GroupMessageReadStatus> findByMessageAndUser(GroupChatMessage message, UserEntity user);

    // 유저가 안 읽은 메시지들
    List<GroupMessageReadStatus> findByUserAndIsReadFalse(UserEntity user); // ✅ 수정됨

    // 채팅방 기준으로 특정 유저의 안읽은 메시지 수
    long countByUser_IdAndIsReadFalseAndMessage_GroupChatRoom_Id(Long userId, Long roomId);

    List<GroupMessageReadStatus> findByUser_IdAndIsReadFalse(Long userId); // ✅ 수정됨
}
