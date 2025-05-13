package com.example.bob.WebSocket;

import com.example.bob.DTO.ChatMessageDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.GroupChatMessageService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class GroupChatWebSocketController {

    private final GroupChatMessageService groupChatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/groupchat.send/{roomId}")
    public void sendGroupMessage(@DestinationVariable Long roomId,
                                 @Payload ChatMessageDTO messageDTO) {

        // 👇 senderId를 기반으로 유저 조회
        Long senderId = messageDTO.getSenderId();
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저 없음"));

        // 저장
        groupChatMessageService.saveMessage(roomId, sender, messageDTO.getMessage());

        // 브로드캐스트
        ChatMessageDTO outbound = ChatMessageDTO.builder()
                .roomId(roomId)
                .senderId(sender.getId())
                .senderName(sender.getUserNick())
                .message(messageDTO.getMessage())
                .sentAt(LocalDateTime.now().toString())
                .build();

        messagingTemplate.convertAndSend("/topic/grouproom." + roomId, outbound);
    }

}
