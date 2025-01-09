package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private UserRepository userRepository; // 사용자 정보를 조회하는 Repository

    @GetMapping("/")
    public String redirectToMain(){
        return "redirect:/main";
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String user_id_login) {
        System.out.println("Received userID: " + user_id_login);  // 사용자 아이디 로그
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsByUserIdLogin(user_id_login); // userID가 이미 존재하는지 확인
        System.out.println("User exists: " + exists);  // 결과 로그
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    //회원가입 페이지 출력 요청 (PostMapping에서 form에 대한 action 수행)
    @GetMapping("/signup")
    public String saveForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String join( @RequestParam String phone1,
                        @RequestParam String phone2,
                        @RequestParam String phone3,
                        @ModelAttribute UserDTO userDTO){

        // phone1, phone2, phone3를 합쳐서 userPhone에 연결
        String userPhone = phone1 + "-" + phone2 + "-" + phone3;
        userDTO.setUserPhone(userPhone);

        //디버깅 메세지
        //System.out.println("UserController.save");
        //System.out.println("userDTO = " + userDTO);

        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // 여기서 로그인 처리 로직을 작성합니다.
        // 예: 사용자의 아이디와 비밀번호를 확인하고 세션을 생성하거나 리다이렉트를 처리합니다.

        if (isValidUser(username, password)) {
            return "redirect:/home"; // 로그인 성공 시 홈 화면으로 이동
        } else {
            return "redirect:/login"; // 로그인 실패 시 다시 로그인 페이지로 리다이렉트
        }
    }

    private boolean isValidUser(String username, String password) {
        // 실제 사용자 인증 로직을 구현
        return "user".equals(username) && "password".equals(password);
    }

    //잠시 html 보기 위해 설정
    @GetMapping("/main")
    public String mainPage(Model model) {
        return "main";
    }

    @GetMapping("/header")
    public String headerPage(Model model) {return "header";}
}

