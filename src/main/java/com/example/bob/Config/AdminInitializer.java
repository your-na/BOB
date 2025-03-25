package com.example.bob.Config;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserService userService;

    @PostConstruct
    public void initAdmin() {
        String adminId = "admin";
        String adminPwd = "admin";

        // 이미 admin 계정이 있는지 확인
        if (!userService.userIdExists(adminId)) {
            UserDTO admin = UserDTO.builder()
                    .userIdLogin(adminId)
                    .pwd(adminPwd)
                    .userNick("관리자")
                    .userName("관리자")
                    .userEmail("BOBManager@gmail.com")
                    .userPhone("x")
                    .sex("x")
                    .accountCreatedAt(LocalDateTime.now())
                    .mainLanguage("x")
                    .birthday("x")
                    .role("ADMIN")
                    .build();

            userService.save(admin);
            System.out.println("✅ 관리자 계정(admin)이 생성되었습니다.");
        } else {
            System.out.println("ℹ️ 관리자 계정(admin)이 이미 존재합니다.");
        }
    }
}
