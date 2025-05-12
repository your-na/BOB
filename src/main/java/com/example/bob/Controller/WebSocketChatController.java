package com.example.bob.Controller;

import com.example.bob.DTO.ChatMessageDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.ChatMessageService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/room.{roomId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long roomId,
                                      @Payload ChatMessageDTO message,
                                      Principal principal) {

        if (principal == null) {
            throw new AccessDeniedException("로그인한 사용자만 사용할 수 있습니다.");
        }

        String username = principal.getName();
        UserEntity sender = userRepository.findByUserIdLogin(username)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        message.setSenderId(sender.getId());  // 사용자 ID 추가
        message.setSenderName(sender.getUserNick());
        message.setRoomId(roomId);

        chatMessageService.saveMessage(message, sender);
        return message;

    }

}
