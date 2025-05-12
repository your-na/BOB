package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    private LocalDateTime createdAt;

    private boolean pinned = false;

    @OneToMany(mappedBy = "groupChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupChatParticipant> participants;
}
