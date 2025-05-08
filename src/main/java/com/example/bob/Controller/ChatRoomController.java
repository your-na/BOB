package com.example.bob.Controller;

import com.example.bob.DTO.ChatRoomSummaryDTO;
import com.example.bob.Entity.PrivateChatRoom;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.PrivateChatRoomRepository;
import com.example.bob.Service.ChatRoomService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final PrivateChatRoomRepository chatRoomRepository;

    @GetMapping("/room")
    public ResponseEntity<Long> getOrCreateRoom(@RequestParam String userA, @RequestParam String userB) {
        Long roomId = chatRoomService.getOrCreateRoom(userA, userB);
        return ResponseEntity.ok(roomId);
    }

    @GetMapping("/api/chat/rooms")
    @ResponseBody
    public ResponseEntity<List<ChatRoomSummaryDTO>> getChatRoomSummaries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getId();
        List<ChatRoomSummaryDTO> summaries = chatRoomService.getChatRoomsWithLastMessages(userId);
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/{roomId}/pin")
    public ResponseEntity<?> pinRoom(@PathVariable Long roomId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.pinRoom(roomId, userDetails.getUserEntity());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chatroom")
    public String chatRoomPage(@RequestParam Long roomId,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               Model model) {
        UserEntity currentUser = userDetails.getUserEntity();

        // 로그인한 사용자 정보를 모델에 담음
        model.addAttribute("user", currentUser);

        PrivateChatRoom room = chatRoomService.findById(roomId);

        UserEntity opponent = room.getUserA().getId().equals(currentUser.getId())
                ? room.getUserB()
                : room.getUserA();

        model.addAttribute("opponent", opponent); // 이 줄이 없으면 오류 발생합니다

        System.out.println("✅ opponent: " + opponent);
        System.out.println("🟢 opponent.getUserNick(): " + opponent.getUserNick());
        System.out.println("🖼 opponent.getProfileImageUrl(): " + opponent.getProfileImageUrl());


        return "chat_room";
    }


}
