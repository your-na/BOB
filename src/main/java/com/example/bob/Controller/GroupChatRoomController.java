package com.example.bob.Controller;

import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.GroupChatParticipantRepository;
import com.example.bob.Repository.GroupChatRoomRepository;
import com.example.bob.Service.GroupChatMessageService;
import com.example.bob.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class GroupChatRoomController {

    private final GroupChatMessageService groupChatMessageService;
    private final GroupChatParticipantRepository groupChatParticipantRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;

    @GetMapping("/group-chatroom")
    public String groupChatRoomPage(@RequestParam Long roomId,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails,
                                    Model model) {
        UserEntity currentUser = userDetails.getUserEntity();
        model.addAttribute("user", currentUser);

        groupChatMessageService.markMessagesAsRead(roomId, currentUser.getId());

        Map<String, Map<String, String>> userMap = groupChatMessageService.getUserInfoMap(roomId);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String userMapJson = objectMapper.writeValueAsString(userMap);
            model.addAttribute("userMapJson", userMapJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("userMapJson", "{}");
        }

        model.addAttribute("roomId", roomId);
        model.addAttribute("chatType", "group");
        model.addAttribute("groupRoom", groupChatRoomRepository.findById(roomId).orElse(null));

        return "chat_room";
    }

}
