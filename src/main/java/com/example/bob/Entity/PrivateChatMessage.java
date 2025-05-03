package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    private String message;

    private LocalDateTime sentAt;
}
