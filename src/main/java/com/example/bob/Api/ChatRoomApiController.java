package com.example.bob.Api;

import com.example.bob.DTO.ChatRoomSummaryDTO;
import com.example.bob.Service.ChatRoomService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomApiController {

    private final ChatRoomService chatRoomService;

    // 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomSummaryDTO>> getChatRoomSummaries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getId();
        List<ChatRoomSummaryDTO> summaries = chatRoomService.getChatRoomsWithLastMessages(userId);
        return ResponseEntity.ok(summaries);
    }

    // 채팅방 상단 고정
    @PostMapping("/room/{roomId}/pin")
    public ResponseEntity<?> pinRoom(@PathVariable Long roomId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.pinRoom(roomId, userDetails.getUserEntity());
        return ResponseEntity.ok().build();
    }
}
