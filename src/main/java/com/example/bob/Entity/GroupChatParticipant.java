package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private GroupChatRoom groupChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    private boolean isAdmin; // 방장 여부
}
