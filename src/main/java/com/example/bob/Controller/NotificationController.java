package com.example.bob.Controller;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.NotificationService;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.security.UserDetailsImpl;
import com.example.bob.security.CustomUserDetails;
import com.example.bob.DTO.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/count")
    public Map<String, Integer> getNotificationCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return Map.of("notificationsCount", 0);

        int count = 0;
        if (userDetails.getUserType().equals("user")){
            UserEntity user = ((UserDetailsImpl) userDetails).getUserEntity();
            count = notificationService.getUserNotificationCount(user);
        } else if (userDetails.getUserType().equals("company")){
            CompanyEntity company = ((CompanyDetailsImpl) userDetails).getCompanyEntity();
            count= notificationService.getCompanyNotificationCount(company);
        }

        return Map.of("notificationsCount", count);
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestParam int page,
                                                      @RequestParam int size) {
        if (!(userDetails instanceof UserDetailsImpl user)) {
            return List.of(); // 기업 사용자 또는 비로그인 시 빈 리스트 반환
        }

        UserEntity userEntity = user.getUserEntity();
        List<NotificationDTO> notificationDTOs = notificationService.getNotifications(userEntity, page, size);

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (NotificationDTO dto : notificationDTOs) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", dto.getId());
            data.put("message", dto.getMessage());
            data.put("isRead", dto.isRead());
            data.put("timestamp", dto.getTimestamp());
            data.put("link", dto.getLink());
            responseList.add(data);
        }

        return responseList;
    }

    @PatchMapping("/mark-as-read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        if (success) {
            return ResponseEntity.ok("알림이 읽음 상태로 변경되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("알림을 찾을 수 없습니다.");
        }
    }
}
