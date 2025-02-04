package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String redirectToMain(){
        return "redirect:/main";
    }

    @GetMapping("/sign")
    public String signPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication.getPrincipal() instanceof String)) {
            return "redirect:/main"; // 로그인 상태라면 메인 페이지로 리디렉션
        }
        return "sign"; // 로그인하지 않으면 sign 페이지로 이동
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";  // 회원가입 페이지로 이동
    }

    @PostMapping("/signup")
    public String join(@RequestParam String phone1,
                       @RequestParam String phone2,
                       @RequestParam String phone3,
                       @ModelAttribute UserDTO userDTO){

        // phone1, phone2, phone3를 합쳐서 userPhone에 연결
        String userPhone = phone1 + "-" + phone2 + "-" + phone3;
        userDTO.setUserPhone(userPhone);  // DTO에 전화번호 세팅

        try {
            System.out.println("UserDTO: " + userDTO); // DTO 디버깅
            // 회원가입 서비스 호출
            userService.save(userDTO);
        } catch (Exception e) {
            System.out.println("회원가입 실패: " + e.getMessage());
            e.printStackTrace();
            return "signup"; // 실패 시 회원가입 페이지로 돌아가기
        }

        return "redirect:/login";  // 성공 시 로그인 페이지로 리디렉션
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String user_id_login) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsByUserIdLogin(user_id_login); // userID가 이미 존재하는지 확인
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkUserNick(@RequestParam String user_nick) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userRepository.existsByUserNick(user_nick);
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateProfileImage")
    public String updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication.getPrincipal() instanceof String)) {
            String username = authentication.getName();
            Optional<UserEntity> optionalUser = userRepository.findByUserIdLogin(username);
            if (optionalUser.isPresent()) {
                UserEntity userEntity = optionalUser.get();
                String fileName = profileImage.getOriginalFilename();
                Path path = Paths.get("static/images/profileImages/" + fileName);
                try {
                    Files.copy(profileImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    userEntity.setProfileImageUrl("/profileImages/" + fileName);
                    userRepository.save(userEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "error";
                }
            }
        }
        return "redirect:/main";
    }

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication.getPrincipal() instanceof String)) {
            return "redirect:/main"; // 이미 로그인 상태라면 메인 페이지로 리디렉션
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        return "redirect:/login"; // 로그아웃 후 로그인 페이지로 이동
    }

    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UserEntity loggedInUser = (UserEntity) authentication.getPrincipal();
        if (!loggedInUser.getUserId().equals(userId)){
            return "redirect:/main";
        }

        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "profile";
        }
        return "redirect:/";
    }

    @GetMapping("/contest")
    public String form() {
        return "project";
    }

    @GetMapping("/bw")
    public String Form() {
        return "newproject";
    }

    @GetMapping("/bowon")
    public String postform() {return "postproject";}
}
