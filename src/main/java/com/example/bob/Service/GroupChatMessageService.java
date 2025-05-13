package com.example.bob.Service;

import com.example.bob.Entity.*;
import com.example.bob.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupChatMessageService {

    private final GroupChatMessageRepository messageRepository;
    private final GroupChatRoomRepository roomRepository;
    private final GroupChatParticipantRepository participantRepository;
    private final GroupMessageReadStatusRepository readStatusRepository;

    @Transactional
    public void saveMessage(Long roomId, UserEntity sender, String messageText) {
        GroupChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        GroupChatMessage message = GroupChatMessage.builder()
                .groupChatRoom(room)
                .sender(sender)
                .message(messageText)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        // 참여자 목록 조회 (보낸 사람 제외)
        List<GroupChatParticipant> participants = participantRepository.findByGroupChatRoom(room);

        for (GroupChatParticipant participant : participants) {
            if (!participant.getUser().getId().equals(sender.getId())) {
                GroupMessageReadStatus status = new GroupMessageReadStatus(message, participant.getUser());
                readStatusRepository.save(status);
            }
        }

        System.out.println("✅ 단체 메시지 저장 및 읽음 상태 초기화 완료");
    }

    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        List<GroupMessageReadStatus> unreadStatuses = readStatusRepository.findByUser_IdAndIsReadFalse(userId);

        List<GroupMessageReadStatus> target = unreadStatuses.stream()
                .filter(status -> status.getMessage().getGroupChatRoom().getId().equals(roomId))
                .toList();

        for (GroupMessageReadStatus status : target) {
            status.markAsRead(); // read = true, readAt = now()
        }

        readStatusRepository.saveAll(target);

        System.out.println("📘 읽음 처리 완료 (" + target.size() + "건)");
    }

    public Map<String, Map<String, String>> getUserInfoMap(Long roomId) {
        List<UserEntity> participants = participantRepository.findUsersByRoomId(roomId);
        List<GroupChatMessage> messages = messageRepository.findWithSenderByRoomId(roomId);

        Map<String, UserEntity> uniqueUsers = new HashMap<>();
        for (UserEntity user : participants) {
            uniqueUsers.put(String.valueOf(user.getId()), user);
        }
        for (GroupChatMessage msg : messages) {
            UserEntity sender = msg.getSender();
            uniqueUsers.put(String.valueOf(sender.getId()), sender);
        }

        Map<String, Map<String, String>> userMap = new HashMap<>();
        for (Map.Entry<String, UserEntity> entry : uniqueUsers.entrySet()) {
            UserEntity user = entry.getValue();
            Map<String, String> info = new HashMap<>();
            info.put("nick", user.getUserNick());
            info.put("image", user.getProfileImageUrl());
            userMap.put(entry.getKey(), info);
        }

        return userMap;
    }
}
