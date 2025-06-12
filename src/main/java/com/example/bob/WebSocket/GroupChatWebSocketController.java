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
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class GroupChatWebSocketController {

    private final GroupChatMessageService groupChatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/groupchat.send/{roomId}")
    public void sendGroupMessage(@DestinationVariable Long roomId,
                                 @Payload ChatMessageDTO messageDTO) {

        Long senderId = messageDTO.getSenderId();
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저 없음"));

        // 보낸 시간 설정
        String nowFormatted = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        messageDTO.setRoomId(roomId);
        messageDTO.setSenderName(sender.getUserNick());
        messageDTO.setSentAt(nowFormatted);

        // ✅ 파일/이미지/텍스트 모든 메시지 저장
        groupChatMessageService.saveMessage(messageDTO, sender);

        // ✅ 그대로 전송
        messagingTemplate.convertAndSend("/topic/grouproom." + roomId, messageDTO);
    }

}
