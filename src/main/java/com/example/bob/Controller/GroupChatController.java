package com.example.bob.Controller;

import com.example.bob.DTO.CreateGroupChatRequest;
import com.example.bob.Service.GroupChatService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    @PostMapping("/group-chat")
    public ResponseEntity<Long> createGroupChat(@RequestBody CreateGroupChatRequest request,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long currentUserId = userDetails.getUserEntity().getId();

        // ⚠ createRoom은 이제 3개 인자 받음 (roomName, userIds, creatorId)
        Long roomId = groupChatService.createRoom(request.getRoomName(), request.getUserIds(), currentUserId);

        return ResponseEntity.ok(roomId);
    }

    @PostMapping("/chat/group/team/{teamId}")
    public ResponseEntity<?> openTeamGroupChat(@PathVariable Long teamId,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getId();

        Long roomId = groupChatService.getOrCreateTeamChatRoom(teamId, userId); // 존재하면 가져오고 없으면 생성
        return ResponseEntity.ok(Map.of("roomId", roomId));
    }

}
