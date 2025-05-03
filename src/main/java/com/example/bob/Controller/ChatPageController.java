package com.example.bob.Controller;

import com.example.bob.security.UserDetailsImpl;
import com.example.bob.Entity.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatPageController {

    @GetMapping("/chatroom")
    public String chatRoomPage(@RequestParam("roomId") Long roomId,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               Model model) {

        UserEntity loginUser = userDetails.getUserEntity();

        model.addAttribute("user", loginUser);
        model.addAttribute("roomId", roomId); // 선택적, JS에서 사용 가능
        return "chat_room"; // templates/chat_room.html
    }
}
