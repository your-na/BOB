package com.example.bob.Service;

import com.example.bob.DTO.ChatRoomSummaryDTO;
import com.example.bob.Entity.*;
import com.example.bob.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final PrivateChatRoomRepository chatRoomRepository;
    private final PrivateChatMessageRepository privateChatMessageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final UserRepository userRepository;
    private final GroupChatParticipantRepository groupChatParticipantRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final GroupMessageReadStatusRepository groupMessageReadStatusRepository;


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

    // 채팅 고정
    public void pinRoom(Long roomId, UserEntity user) {
        PrivateChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        if (room.getUserA().getId().equals(user.getId())) {
            room.setPinnedByA(!room.isPinnedByA()); // 토글
        } else if (room.getUserB().getId().equals(user.getId())) {
            room.setPinnedByB(!room.isPinnedByB()); // 토글
        }

        chatRoomRepository.save(room);
    }


    public PrivateChatRoom findById(Long roomId) {
        return chatRoomRepository.findByIdWithUsers(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
    }

    public List<ChatRoomSummaryDTO> getChatRoomsWithLastMessages(Long userId) {
        List<PrivateChatRoom> rooms = chatRoomRepository.findAllByUserId(userId);

        return rooms.stream()
                .map(room -> {
                    UserEntity opponent = room.getUserA().getId().equals(userId)
                            ? room.getUserB()
                            : room.getUserA();

                    PrivateChatMessage lastMsg = privateChatMessageRepository.findTopByRoomIdOrderBySentAtDesc(room.getId());

                    long unreadCount = messageReadStatusRepository
                            .countByUser_IdAndIsReadFalseAndMessage_RoomId(userId, room.getId());


                    return ChatRoomSummaryDTO.builder()
                            .roomId(room.getId())
                            .opponentNick(opponent.getUserNick())
                            .opponentProfileUrl(opponent.getProfileImageUrl())
                            .lastMessage(lastMsg != null ? lastMsg.getMessage() : "(대화를 시작해보세요)")
                            .lastMessageTime(lastMsg != null ? lastMsg.getSentAt() : LocalDateTime.MIN)
                            .build();
                })
                .sorted(Comparator.comparing(ChatRoomSummaryDTO::getLastMessageTime).reversed())
                .toList();
    }

    public List<ChatRoomSummaryDTO> getUnifiedChatRoomSummaries(Long userId) {
        List<ChatRoomSummaryDTO> result = new ArrayList<>();

        // ✅ 1:1 채팅방 목록
        List<PrivateChatRoom> privateRooms = chatRoomRepository.findAllByUserId(userId);
        for (PrivateChatRoom room : privateRooms) {
            UserEntity opponent = room.getUserA().getId().equals(userId) ? room.getUserB() : room.getUserA();
            PrivateChatMessage lastMsg = privateChatMessageRepository.findTopByRoomIdOrderBySentAtDesc(room.getId());
            long unread = messageReadStatusRepository
                    .countByUser_IdAndIsReadFalseAndMessage_RoomId(userId, room.getId());

            result.add(ChatRoomSummaryDTO.builder()
                    .roomId(room.getId())
                    .opponentNick(opponent.getUserNick())
                    .opponentProfileUrl(opponent.getProfileImageUrl())
                    .lastMessage(lastMsg != null ? lastMsg.getMessage() : "(대화를 시작하세요)")
                    .lastMessageTime(lastMsg != null ? lastMsg.getSentAt() : LocalDateTime.MIN)
                    .unreadCount((int) unread)
                    .chatType("private")
                    .pinned(room.getUserA().getId().equals(userId) ? room.isPinnedByA() : room.isPinnedByB())
                    .build());
        }

        // ✅ 단체 채팅방 목록
        List<GroupChatParticipant> groupParticipants = groupChatParticipantRepository.findByUser_Id(userId);
        for (GroupChatParticipant p : groupParticipants) {
            GroupChatRoom room = p.getGroupChatRoom();
            GroupChatMessage lastMsg = groupChatMessageRepository.findTopByGroupChatRoomOrderBySentAtDesc(room);
            long unread = groupMessageReadStatusRepository
                    .countByUser_IdAndIsReadFalseAndMessage_GroupChatRoom_Id(userId, room.getId());

            result.add(ChatRoomSummaryDTO.builder()
                    .roomId(room.getId())
                    .opponentNick(room.getRoomName()) // 방 이름
                    .opponentProfileUrl("/images/group.png") // 그룹 기본 이미지
                    .lastMessage(lastMsg != null ? lastMsg.getMessage() : "(대화를 시작하세요)")
                    .lastMessageTime(lastMsg != null ? lastMsg.getSentAt() : LocalDateTime.MIN)
                    .unreadCount((int) unread)
                    .chatType("group")
                    .pinned(room.isPinned()) // 단체 채팅도 상단 고정 가능
                    .build());
        }

        // ✅ 정렬: 고정된 방 우선, 이후 최근 활동 순
        return result.stream()
                .sorted(Comparator
                        .comparing(ChatRoomSummaryDTO::isPinned).reversed()
                        .thenComparing(ChatRoomSummaryDTO::getLastMessageTime).reversed())
                .toList();
    }

}
