package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Autowired
    private UserRepository userRepository; // 사용자 정보를 조회하는 Repository

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String userIdLogin) {
        System.out.println("Received userID: " + userIdLogin);  // 사용자 아이디 로그
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsById(userIdLogin); // userID가 이미 존재하는지 확인
        System.out.println("User exists: " + exists);  // 결과 로그
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

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

        return "redirect:/login";
    }
}
