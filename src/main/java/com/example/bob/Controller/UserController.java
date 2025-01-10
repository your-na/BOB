package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String user_id_login) {
        System.out.println("Received userID: " + user_id_login);  // 사용자 아이디 로그
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsByUserIdLogin(user_id_login); // userID가 이미 존재하는지 확인
        System.out.println("User exists: " + exists);  // 결과 로그
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkUserNick(@RequestParam String user_nick) {
        System.out.println("Received nickname: " + user_nick); // 닉네임 로그
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsByUserNick(user_nick); // 닉네임 중복 확인
        System.out.println("Nickname exists: " + exists);  // 결과 로그
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateProfileImage")
    public String updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage, HttpSession session){
        // 현재 로그인한 사용자 정보 가져오기
        UserEntity userEntity = (UserEntity) session.getAttribute("user");

        if (userEntity != null){
            // 파일 업로드 처리
            String fileName = profileImage.getOriginalFilename();;
            Path path = Paths.get("static/profileImages/" + fileName);

            try {
                Files.copy(profileImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                // DB에 저장된 사용자 프로필 이미지 URL 업데이트
                userEntity.setProfileImageUrl("/profileImages/" + fileName);
                userRepository.save(userEntity); // DB에 업데이트
                session.setAttribute("user", userEntity); // 세션 업데이트
            } catch (IOException e){
                e.printStackTrace();
                return "error";
            }
        }
        return "redirect:/main";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        // 디버깅: 입력된 사용자 정보 로그
        System.out.println("로그인 시도 - 아이디: " + username + ", 비밀번호: " + password);

        // 데이터베이스에서 사용자 정보 조회
        Optional<UserEntity> optionalUser = userRepository.findByUserIdLogin(username);

        //사용자 존재 여부 확인 및 비밀번호 검증
        if (optionalUser.isPresent()) {
            // 디버깅: 사용자 존재 여부 확인
            System.out.println("사용자 존재 확인 - 아이디: " + username);

            UserEntity userEntity = optionalUser.get();
            if (userService.getPasswordEncoder().matches(password, userEntity.getPwd())) {
                // 디버깅: 비밀번호 일치
                System.out.println("비밀번호 일치 - 로그인 성공: " + username);

                // 인증 성공 : 사용자 정보를 세션에 저장
                session.setAttribute("user", userEntity);
                return "redirect:/main"; // 로그인 성공 시 홈 화면으로 이동
            } else {
                // 디버깅: 비밀번호 불일치
                System.out.println("비밀번호 불일치 - 로그인 실패: " + username);
            }
        } else {
            // 디버깅: 사용자 존재하지 않음
            System.out.println("사용자 존재하지 않음 - 아이디: " + username);
        }

        // 로그인 실패 시 로그인 페이지로 리다이렉트 (에러 표시)
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션에서 사용자 정보 제거
        session.invalidate();
        return "redirect:/login"; // 로그아웃 후 로그인 페이지로 이동
    }

    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model, HttpSession session) {
        // 세션에서 현재 로그인된 사용자 정보 가져오기
        UserEntity loggedInUser = (UserEntity) session.getAttribute("user");

        // 로그인한 사용자만 자신의 프로필을 볼 수 있도록 검사
        if (loggedInUser == null || !loggedInUser.getUserId().equals(userId)) {
            // 로그인 안된 경우나, 다른 사용자의 프로필에 접근하려는 경우
            return "redirect:/login";  // 로그인 페이지로 리다이렉트
        }

        Optional<UserEntity> user = userRepository.findById(userId);

        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "profile";  // 자신의 프로필 페이지로 이동
        } else {
            return "redirect:/";  // 사용자가 존재하지 않으면 홈 페이지로 리다이렉트
        }
    }


    //잠시 html 보기 위해 설정
    @GetMapping("/main")
    public String mainPage(Model model, HttpSession session) {
        // 세션에서 사용자 정보 가져오기
        UserEntity user = (UserEntity) session.getAttribute("user");

        if (user != null) {
            // 사용자 정보 모델에 추가
            model.addAttribute("user", user);
            model.addAttribute("profileLink", "/profile/" + user.getUserId());
        }
        else {
            model.addAttribute("profileLink", "#");
        }

        return "main";
    }
    //회원가입 초기화면 입니둥
    @GetMapping("/sign")
    public String Form() {
        return "sign";
    }

    //이건 공모전 목록 test용이라서 나중에 삭제하고 해도 돼용
    @GetMapping("/contest")
    public String form() {
        return "contest";
    }
}

