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
        UserEntity currentUser = userDetails.getUserEntity();

        // 로그인한 사용자 정보를 모델에 담음
        model.addAttribute("user", currentUser);

        PrivateChatRoom room = chatRoomService.findById(roomId);
        UserEntity opponent = room.getUserA().getId().equals(currentUser.getId())
                ? room.getUserB()
                : room.getUserA();

        model.addAttribute("opponent", opponent); // 이 줄이 없으면 오류 발생합니다

//        model.addAttribute("currentUserNick", currentUser.getUserNick());
//        model.addAttribute("currentUserId", currentUser.getId());
//        model.addAttribute("currentProfileImage", currentUser.getProfileImageUrl());
        return "chat_room";
    }


}
