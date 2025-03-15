package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {

    private Long id;  // 알림 ID
    private String message;  // 알림 메시지
    private boolean isRead;  // 읽음 여부
    private LocalDateTime timestamp;  // 알림 시간
    private UserDTO user;  // 알림을 받은 사용자 (UserDTO로 정의)
    private UserDTO sender;  // 알림을 보낸 사용자 (UserDTO로 정의)
    private ProjectDTO project;  // 알림과 관련된 프로젝트 (ProjectDTO로 정의)
    private String link;  // 알림과 연결된 링크

    // 생성자
    public NotificationDTO(Long id, String message, boolean isRead, LocalDateTime timestamp,
                           UserDTO user, UserDTO sender, ProjectDTO project, String link) {
        this.id = id;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.user = user;
        this.sender = sender;
        this.project = project;
        this.link = link;
    }

    // 기본 생성자 (Lombok이 제공하는 기본 생성자)
    public NotificationDTO() {}
}
