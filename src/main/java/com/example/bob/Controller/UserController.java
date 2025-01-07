package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //회원가입 페이지 출력 요청 (PostMapping에서 form에 대한 action 수행)
    @GetMapping("/Test2")
    public String saveForm(){
        return "Test2";
    }

    @PostMapping("/Test2")
    public String join(@ModelAttribute UserDTO userDTO){
        System.out.println("UserController.save");
        System.out.println("userDTO = " + userDTO);
        userService.save(userDTO);

        return "Test1";
    }
}
