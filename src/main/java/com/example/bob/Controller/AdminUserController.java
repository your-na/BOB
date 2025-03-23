package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/admin/company")
    public String adcomform(Model model) {
        model.addAttribute("companyList", Page.empty()); // 임시로 빈 페이지 넣기
        return "ad_company_list";
    }


}
