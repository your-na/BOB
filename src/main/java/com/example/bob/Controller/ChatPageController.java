package com.example.bob.Controller;

import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ChatPageController {

    @GetMapping("/chatting")
    public String chatPopupPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // 로그인한 사용자 정보 넘겨주기 (채팅방 목록 표시 시 필요할 수 있음)
        model.addAttribute("user", userDetails.getUserEntity());
        return "chat_popup"; // templates/chat_popup.html
    }
}
