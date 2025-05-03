package com.example.bob.Controller;

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

    @GetMapping("/chatroom")
    public String chatRoomPage(@RequestParam Long roomId,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               Model model) {
        // 로그인한 사용자 정보를 모델에 담음
        model.addAttribute("user", userDetails.getUserEntity());

        // 채팅방 정보도 함께 넘기고자 할 경우
        PrivateChatRoom room = chatRoomService.findById(roomId);
        UserEntity currentUser = userDetails.getUserEntity();

        // 상대방 닉네임만 넘기기
        String opponentNick = room.getUserA().getId().equals(currentUser.getId())
                ? room.getUserB().getUserNick()
                : room.getUserA().getUserNick();
        model.addAttribute("opponentNick", opponentNick);

        return "chat_room";
    }


}
