package com.example.bob.Service;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.NotificationEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.NotificationRepository;
import com.example.bob.DTO.NotificationDTO;
import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.ProjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.example.bob.Entity.NotificationType;



@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository; // UserRepository 주입

    // 알림 수를 계산하는 메서드
    public int getUserNotificationCount(UserEntity userEntity) {
        return notificationRepository.countByUserAndIsRead(userEntity, false); // 사용자가 읽지 않은 알림 수
    }

    // 기업 사용자 알림 수 조회
    public int getCompanyNotificationCount(CompanyEntity company) {
        return notificationRepository.countByCompanyAndIsRead(company , false); // 기업이 읽지 않은 알림 수
    }

    // 알림 목록을 가져오는 메서드 (프로젝트/공모전 팀 알림 모두 지원)
    public List<NotificationDTO> getNotifications(UserEntity userEntity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> notificationsPage = notificationRepository.findByUser(userEntity, pageable);

        return notificationsPage.getContent().stream()
                .map(notification -> {
                    ProjectDTO projectDTO = null;
                    if (notification.getProject() != null) {
                        projectDTO = new ProjectDTO(
                                notification.getProject().getId(),
                                notification.getProject().getTitle(),
                                notification.getProject().getCreatedBy(),
                                notification.getProject().getDescription(),
                                notification.getProject().getGoal(),
                                notification.getProject().getStartDate(),
                                notification.getProject().getEndDate(),
                                notification.getProject().getRecruitmentCount(),
                                notification.getProject().getCurrentParticipants(),
                                notification.getProject().getViews(),
                                notification.getProject().getLikes(),
                                notification.getProject().getStatus(),
                                notification.getProject().getRecruitmentPeriod(),
                                notification.getProject().getRecruitmentEndDate(),
                                notification.getProject().getRecruitmentStartDate()
                        );
                    }

                    return new NotificationDTO(
                            notification.getId(),
                            notification.getMessage(),
                            notification.isRead(),
                            notification.getTimestamp(),
                            new UserDTO(notification.getUser().getUserId(), notification.getUser().getUserName()),
                            new UserDTO(notification.getSender().getUserId(), notification.getSender().getUserName()),
                            projectDTO,
                            notification.getLink()
                    );
                })
                .collect(Collectors.toList());
    }


    // 알림을 읽음 상태로 변경하는 메서드
    public boolean markAsRead(Long id) {
        NotificationEntity notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);  // 읽음 상태로 변경
            notificationRepository.save(notification);  // 변경된 알림 저장
            return true; // 성공적으로 읽음 상태로 변경됨
        }
        return false; // 알림을 찾을 수 없으면 false 반환
    }

    // 알림 생성하는 메서드
    public void createNotification(UserEntity userEntity, ProjectEntity project, Long senderId) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("보낸 사람을 찾을 수 없습니다."));

        NotificationEntity notification = new NotificationEntity();
        notification.setUser(userEntity);  // 알림을 받을 사용자 설정
        notification.setSender(sender);  // 발송자 설정
        notification.setMessage(sender.getUserNick() + "님이 " + project.getTitle() + " 프로젝트에 팀원 신청을 보냈습니다.");  // 메시지 설정
        notification.setProject(project);  // 해당 프로젝트 설정
        notification.setTimestamp(LocalDateTime.now());  // 알림 시간
        notification.setIsRead(false);  // 읽지 않은 상태로 설정
        notification.setType(NotificationType.PROJECT_INVITE);
        notificationRepository.save(notification);  // 알림 저장
    }

    // 모든 알림 삭제
    @Transactional
    public void deleteAllNotificationsForUser(UserEntity userEntity) {
        notificationRepository.deleteByUser(userEntity);  // 사용자에 해당하는 모든 알림 삭제
    }
}
