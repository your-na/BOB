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
public class PrivateChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserEntity userA;

    @ManyToOne
    private UserEntity userB;

    @Column(nullable = false)
    private boolean pinnedByA = false;

    @Column(nullable = false)
    private boolean pinnedByB = false;

    private LocalDateTime createdAt;

}
