package com.example.bob.Service;

import com.example.bob.DTO.ChatMessageDTO;
import com.example.bob.Entity.MessageReadStatus;
import com.example.bob.Entity.PrivateChatMessage;
import com.example.bob.Entity.PrivateChatRoom;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.ChatMessageRepository;
import com.example.bob.Repository.MessageReadStatusRepository;
import com.example.bob.Repository.PrivateChatRoomRepository;
import com.example.bob.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final PrivateChatRoomRepository privateChatRoomRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final UserRepository userRepository;


    public List<ChatMessageDTO> getMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId).stream()
                .map(msg -> ChatMessageDTO.builder()
                        .roomId(roomId)
                        .senderId(msg.getSender().getId())
                        .senderName(msg.getSender().getUserNick())
                        .message(msg.getMessage())
                        .sentAt(msg.getSentAt().toString())
                        .type(msg.getType())
                        .fileUrl(msg.getFileUrl())
                        .fileName(msg.getFilename())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveMessage(ChatMessageDTO dto, UserEntity sender) {
        System.out.println("💾 [1] 저장 시도");
        System.out.println("💾 [2] sender: " + sender);
        System.out.println("💾 [3] senderId: " + sender.getUserId());
        System.out.println("💾 [4] roomId: " + dto.getRoomId());

        PrivateChatMessage message = PrivateChatMessage.builder()
                .roomId(dto.getRoomId())
                .sender(sender)
                .message(dto.getMessage())
                .sentAt(LocalDateTime.now())
                .type(dto.getType())
                .fileUrl(dto.getFileUrl())
                .filename(dto.getFileName())
                .build();

        chatMessageRepository.saveAndFlush(message);
        System.out.println("✅ [5] 저장 완료 후 flush");

        PrivateChatRoom room = privateChatRoomRepository.findByIdWithUsers(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        UserEntity receiver = !sender.getId().equals(room.getUserA().getId())
                ? room.getUserA()
                : room.getUserB();

        MessageReadStatus readStatus = new MessageReadStatus(message, receiver);
        messageReadStatusRepository.save(readStatus);

        System.out.println("📍 [6] MessageReadStatus 저장 완료 → receiver: " + receiver.getUserNick());
    }

    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId) {
        // 유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 해당 유저가 아직 읽지 않은 메시지들 조회
        List<MessageReadStatus> unreadStatuses = messageReadStatusRepository
                .findByUserAndIsReadFalse(user);

        // 해당 채팅방의 메시지만 필터링
        List<MessageReadStatus> targetStatuses = unreadStatuses.stream()
                .filter(status -> status.getMessage().getRoomId().equals(roomId))
                .toList();

        // 읽음 처리
        targetStatuses.forEach(MessageReadStatus::markAsRead);
        messageReadStatusRepository.saveAll(targetStatuses);

        System.out.println("✅ 읽음 처리 완료 (" + targetStatuses.size() + "건)");
    }
}
