package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    public String getAllUsers(Model model, Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        model.addAttribute("users", users);
        return "ad_general_user"; // ✅ Thymeleaf 템플릿 파일명
    }
}
