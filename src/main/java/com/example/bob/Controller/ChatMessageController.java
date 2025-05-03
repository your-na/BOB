package com.example.bob.Controller;

import com.example.bob.DTO.ChatMessageDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ChatMessageService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@RequestParam Long roomId) {
        return ResponseEntity.ok(chatMessageService.getMessagesByRoomId(roomId));
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestBody ChatMessageDTO dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity sender = userDetails.getUserEntity();
        chatMessageService.saveMessage(dto, sender);
        return ResponseEntity.ok().build();
    }
}
