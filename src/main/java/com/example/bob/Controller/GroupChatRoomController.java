package com.example.bob.Controller;

import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.GroupChatMessageService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class GroupChatRoomController {

    private final GroupChatMessageService groupChatMessageService;

    @GetMapping("/group-chatroom")
    public String groupChatRoomPage(@RequestParam Long roomId,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails,
                                    Model model) {
        UserEntity currentUser = userDetails.getUserEntity();
        model.addAttribute("user", currentUser);

        // ✅ 읽음 처리
        groupChatMessageService.markMessagesAsRead(roomId, currentUser.getId());

        // group_chat_room.html 렌더링
        model.addAttribute("roomId", roomId);
        model.addAttribute("chatType", "group");

        return "chat_room";
    }
}
