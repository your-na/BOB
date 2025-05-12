package com.example.bob.Controller;

import com.example.bob.DTO.CreateGroupChatRequest;
import com.example.bob.Service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    @PostMapping("/group-chat")
    public ResponseEntity<Long> createGroupChat(@RequestBody CreateGroupChatRequest request) {
        Long roomId = groupChatService.createRoom(request.getRoomName(), request.getUserIds());
        return ResponseEntity.ok(roomId);
    }
}
