package com.example.bob.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;

@Controller
public class HeaderController {

    @ModelAttribute("user")
    public UserDTO getCurrentUser() {
        // Spring Security의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // 인증 정보에서 UserEntity를 가져옴
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();

            // 콘솔에 로그인된 사용자 정보 출력
            System.out.println("로그인된 사용자: " + userEntity.getUserIdLogin() + " (" + userEntity.getUserNick() + ")");

            // UserEntity -> UserDTO로 변환
            return UserDTO.toUserDTO(userEntity); // 수정된 부분
        }
        return null; // 인증되지 않은 사용자일시
    }

    @ModelAttribute("profileLink")
    public String getProfileLink(@ModelAttribute("user") UserDTO user) {
        if (user != null) {
            return "/profile"; // 프로필 페이지로 리디렉션
        }
        return "/login"; // 인증되지 않은 경우 로그인 페이지로 리디렉션
    }
}
