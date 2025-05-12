package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private GroupChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "is_read")
    private boolean isRead = false;

    private LocalDateTime readAt;

    public GroupMessageReadStatus(GroupChatMessage message, UserEntity user) {
        this.message = message;
        this.user = user;
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}

