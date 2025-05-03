package com.example.bob.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String message;
    private String sentAt;
}
