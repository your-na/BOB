package com.example.bob.Controller;

import com.example.bob.DTO.ChatMessageDTO;
import com.example.bob.Entity.GroupChatMessage;
import com.example.bob.Repository.GroupChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupChatMessageController {

    private final GroupChatMessageRepository groupChatMessageRepository;

    @GetMapping("/messages")
    public List<ChatMessageDTO> getMessages(@RequestParam Long roomId) {
        List<GroupChatMessage> messages = groupChatMessageRepository.findByGroupChatRoom_IdOrderBySentAtAsc(roomId);

        return messages.stream().map(msg -> ChatMessageDTO.builder()
                .roomId(roomId)
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getUserNick())
                .message(msg.getMessage())
                .sentAt(msg.getSentAt().toString())
                .build()).toList();
    }
}

