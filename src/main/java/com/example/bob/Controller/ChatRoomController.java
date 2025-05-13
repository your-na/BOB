package com.example.bob.Controller;

import com.example.bob.DTO.ChatRoomSummaryDTO;
import com.example.bob.Entity.GroupChatRoom;
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
import com.example.bob.Service.ChatMessageService;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final PrivateChatRoomRepository chatRoomRepository;

    @GetMapping("/room")
    public ResponseEntity<Long> getOrCreateRoom(@RequestParam String userA, @RequestParam String userB) {
        Long roomId = chatRoomService.getOrCreateRoom(userA, userB);
        return ResponseEntity.ok(roomId);
    }

    @GetMapping("/chatroom")
    public String chatRoomPage(@RequestParam Long roomId,
                               @RequestParam(required = false, defaultValue = "private") String type,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               Model model) {
        UserEntity currentUser = userDetails.getUserEntity();

        // 로그인한 사용자 정보를 모델에 담음
        model.addAttribute("user", currentUser);

        if ("group".equals(type)) {
            // 그룹 채팅 처리
            GroupChatRoom room = chatRoomService.findGroupRoomById(roomId);
            List<UserEntity> members = chatRoomService.getGroupMembers(roomId);

            model.addAttribute("opponent", null); // 그룹은 상대가 없음
            model.addAttribute("chatType", "group");
            model.addAttribute("groupRoom", room);
            model.addAttribute("groupMembers", members);

            return "chat_room";
        } else {
            // 기존의 1:1 채팅 처리
            chatMessageService.markMessagesAsRead(roomId, currentUser.getId());
            PrivateChatRoom room = chatRoomService.findById(roomId);

            UserEntity opponent = room.getUserA().getId().equals(currentUser.getId())
                    ? room.getUserB()
                    : room.getUserA();

            model.addAttribute("opponent", opponent);
            model.addAttribute("chatType", "private");
            return "chat_room";
        }
    }


}
