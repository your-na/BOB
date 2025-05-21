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
import com.example.bob.Entity.CoJobPostEntity;




@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository; // UserRepository 주입

    // 알림 수를 계산하는 메서드
    public int getUserNotificationCount(UserEntity userEntity) {
        return notificationRepository.countByUserAndIsReadFalseAndIsHiddenFalse(userEntity); // 사용자가 읽지 않은 알림 수
    }

    // 기업 사용자 알림 수 조회
    public int getCompanyNotificationCount(CompanyEntity company) {
        return notificationRepository.countByCompanyAndIsRead(company , false); // 기업이 읽지 않은 알림 수
    }

    public List<NotificationDTO> getNotifications(UserEntity userEntity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> notificationsPage = notificationRepository.findByUserAndIsHiddenFalse(userEntity, pageable);

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

                    UserDTO senderDTO = null;
                    if (notification.getSender() != null) {
                        senderDTO = new UserDTO(notification.getSender().getUserId(), notification.getSender().getUserName());
                    }

                    NotificationDTO dto = new NotificationDTO(
                            notification.getId(),
                            notification.getMessage(),
                            notification.isRead(),
                            notification.getTimestamp(),
                            new UserDTO(notification.getUser().getUserId(), notification.getUser().getUserName()),
                            senderDTO,
                            projectDTO,
                            notification.getLink()
                    );

                    // ✅ HIRE_NOTICE 처리
                    if (notification.getType() == NotificationType.HIRE_NOTICE && notification.getJobPost() != null) {
                        dto.setType("HIRE_NOTICE");
                        dto.setJobPostId(notification.getJobPost().getId());
                        if (notification.getCompany() != null) {
                            dto.setCompanyName(notification.getCompany().getCoNick());
                        }
                        dto.setLink("/job/detail/" + notification.getJobPost().getId());
                    }

                    // ✅ PROJECT_INVITE 처리
                    else if (notification.getType() == NotificationType.PROJECT_INVITE && notification.getProject() != null) {
                        dto.setType("PROJECT_INVITE");
                        dto.setProjectId(notification.getProject().getId());
                        dto.setProjectTitle(notification.getProject().getTitle());

                        if (notification.getSender() != null) {
                            dto.setSender(new UserDTO(
                                    notification.getSender().getUserId(),
                                    notification.getSender().getUserName()
                            ));
                            dto.setLink("/teamrequest?projectId=" + notification.getProject().getId()
                                    + "&userId=" + notification.getSender().getUserId());
                        } else {
                            dto.setLink("/teamrequest?projectId=" + notification.getProject().getId());
                        }
                    }


                    // ✅ CONTEST_INVITE 처리
                    else if (notification.getType() == NotificationType.CONTEST_INVITE && notification.getContestTeam() != null) {
                        dto.setType("CONTEST_INVITE");
                        dto.setTeamId(notification.getContestTeam().getId());
                        dto.setTeamName(notification.getContestTeam().getTeamName());
                        dto.setContestId(notification.getContestTeam().getContest().getId());
                        dto.setLink(null);
                    }

                    // ✅ 기타 타입
                    else {
                        dto.setType(notification.getType().name());
                    }

                    return dto;
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
        List<NotificationEntity> list = notificationRepository.findByUserAndIsHiddenFalse(userEntity);
        for (NotificationEntity n : list) {
            n.setHidden(true);
        }
        notificationRepository.saveAll(list);
    }

    // ✅ 채용 합격 알림 생성 메서드
    public void sendHireNotification(UserEntity receiver, CompanyEntity company, String message, CoJobPostEntity jobPost) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(receiver);               // 👤 알림 받을 사용자
        notification.setCompany(company);             // 🏢 기업
        notification.setMessage(message);             // 💬 전달 메시지
        notification.setTimestamp(LocalDateTime.now());
        notification.setIsRead(false);                // 읽지 않음 상태
        notification.setType(NotificationType.HIRE_NOTICE); // 📌 알림 유형
        notification.setJobPost(jobPost);             // 💼 공고 연결
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotificationByTeamAndUser(Long teamId, UserEntity user) {
        List<NotificationEntity> list = notificationRepository.findByContestTeamIdAndUserAndIsHiddenFalse(teamId, user);
        for (NotificationEntity n : list) {
            n.setHidden(true);
        }
        notificationRepository.saveAll(list);
    }

    @Transactional
    public void hideNotification(Long id, UserEntity user) {
        NotificationEntity notification = notificationRepository.findById(id).orElse(null);
        if (notification != null && notification.getUser().getUserId().equals(user.getUserId())) {
            notification.setHidden(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void hideProjectNotifications(ProjectEntity project) {
        notificationRepository.hideByProject(project);
    }


}
