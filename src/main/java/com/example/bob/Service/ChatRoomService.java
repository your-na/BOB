package com.example.bob.Service;

import com.example.bob.Entity.PrivateChatRoom;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.PrivateChatRoomRepository;
import com.example.bob.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final PrivateChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public Long getOrCreateRoom(String userNickA, String userNickB) {
        if (userNickA.equals(userNickB)) {
            throw new IllegalArgumentException("자기 자신과 채팅할 수 없습니다.");
        }

        UserEntity userA = userRepository.findByUserNick(userNickA)
                .orElseThrow(() -> new IllegalArgumentException("유저A 없음"));
        UserEntity userB = userRepository.findByUserNick(userNickB)
                .orElseThrow(() -> new IllegalArgumentException("유저B 없음"));

        return chatRoomRepository
                .findByUserAAndUserBOrUserBAndUserA(userA, userB, userA, userB)
                .map(PrivateChatRoom::getId)
                .orElseGet(() -> {
                    PrivateChatRoom newRoom = PrivateChatRoom.builder()
                            .userA(userA)
                            .userB(userB)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return chatRoomRepository.save(newRoom).getId();
                });
    }

    public PrivateChatRoom findById(Long roomId) {
        return chatRoomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
    }

}
