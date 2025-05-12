package com.example.bob.Service;

import com.example.bob.Entity.GroupChatParticipant;
import com.example.bob.Entity.GroupChatRoom;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.GroupChatParticipantRepository;
import com.example.bob.Repository.GroupChatRoomRepository;
import com.example.bob.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRoomRepository roomRepository;
    private final GroupChatParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createRoom(String roomName, List<Long> userIds) {
        GroupChatRoom room = GroupChatRoom.builder()
                .roomName(roomName)
                .createdAt(LocalDateTime.now())
                .build();
        roomRepository.save(room);

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음: ID=" + userId));

            GroupChatParticipant participant = GroupChatParticipant.builder()
                    .groupChatRoom(room)
                    .user(user)
                    .isAdmin(i == 0) // 첫 번째 유저를 방장으로 설정
                    .build();

            participantRepository.save(participant);
        }

        return room.getId();
    }
}
