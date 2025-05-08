package com.example.bob.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomSummaryDTO {
    private Long roomId;
    private String opponentNick;
    private String opponentProfileUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
