package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateGroupChatRequest {
    private String roomName;       // 생성할 채팅방 이름
    private List<Long> userIds;    // 참여할 사용자 ID 리스트 (본인 포함)
}
