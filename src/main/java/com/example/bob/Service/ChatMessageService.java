package com.example.bob.Service;

import com.example.bob.DTO.ChatMessageDTO;
import com.example.bob.Entity.PrivateChatMessage;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.ChatMessageRepository;
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

    public List<ChatMessageDTO> getMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId).stream()
                .map(msg -> ChatMessageDTO.builder()
                        .roomId(roomId)
                        .senderId(msg.getSender().getId())
                        .senderName(msg.getSender().getUserNick())
                        .message(msg.getMessage())
                        .sentAt(msg.getSentAt().toString())
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
                .build();

        chatMessageRepository.saveAndFlush(message);
        System.out.println("✅ [5] 저장 완료 후 flush");
    }
}
