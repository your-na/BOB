package com.example.bob.Api;

import com.example.bob.DTO.ChatRoomRequestDTO;
import com.example.bob.DTO.ChatRoomSummaryDTO;
import com.example.bob.Service.ChatRoomService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.ProjectService;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.GroupChatRoom;


import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomApiController {

    private final ChatRoomService chatRoomService;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    // 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomSummaryDTO>> getChatRoomSummaries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getId();
        List<ChatRoomSummaryDTO> summaries = chatRoomService.getUnifiedChatRoomSummaries(userId);
        return ResponseEntity.ok(summaries);
    }

    // 1:1 채팅방 생성 또는 조회
    @PostMapping("/room/create")
    public ResponseEntity<Long> createOrGetRoom(@RequestBody ChatRoomRequestDTO dto) {
        Long roomId = chatRoomService.getOrCreateRoom(dto.getUserNickA(), dto.getUserNickB());
        return ResponseEntity.ok(roomId);
    }


    // 채팅방 상단 고정
    @PostMapping("/room/{roomId}/pin")
    public ResponseEntity<?> pinRoom(@PathVariable Long roomId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        chatRoomService.pinRoom(roomId, userDetails.getUserEntity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/group/project/{projectId}")
    public ResponseEntity<Long> createOrGetProjectGroupChat(@PathVariable Long projectId,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();

        // 1. 프로젝트 정보 조회
        ProjectEntity project = projectService.getProjectById(projectId);

        // 2. 참여자 닉네임 리스트 조회
        List<String> memberNicks = projectService.getProjectMemberNicknames(project.getTitle());

        // 3. 닉네임을 기반으로 UserEntity 리스트 구성
        List<UserEntity> members = memberNicks.stream()
                .map(nick -> userRepository.findByUserNick(nick)
                        .orElseThrow(() -> new IllegalArgumentException("❌ 사용자 없음: " + nick)))
                .collect(Collectors.toList());

        // 4. 채팅방 생성 or 조회
        GroupChatRoom room = chatRoomService.getOrCreateGroupRoomByProject(project, members);

        return ResponseEntity.ok(room.getId());
    }
}
