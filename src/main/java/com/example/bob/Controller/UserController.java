package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;

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

        boolean existsInUsers = userRepository.existsByUserIdLogin(user_id_login); // userID가 이미 존재하는지 확인
        boolean existsInCompanies = companyRepository.existsByCoIdLogin(user_id_login); //기업 사용자
        response.put("exists", existsInUsers || existsInCompanies); // 두가지 경우 모두 확인
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


    @GetMapping("/bowon")
    public String postform() {return "postproject";}

    @GetMapping("/job")
    public String jobform() {return "job";}
    //공모전 임의 주소 여기다 할 예정
    @GetMapping("/postcontest")
    public String postcform() {return "postcontest";}
    @GetMapping("/contestact")
    public String conactform() {return "contestact";}
    @GetMapping("/likedcontest")
    public String likedform() {return "liked_contest";}
    @GetMapping("/conhistory")
    public String conhistoryform() {return "contest_history";}

    //관리자 공모전 임의 주소




    //관리자^_^
    @GetMapping("/sidebar")
    public String adsideform() {return "ad_sidebar";}

    @GetMapping("adcomrequest")
    public String adrequestform() {return "ad_company_request";}

    //기업 회원가입&메인 작업중 임의 주소
    @GetMapping("/cosignup")
    public String cosignupform() {return "co_signup";}

    @GetMapping("/header2")
    public String header2form() {return "header2";}

    @GetMapping("/newcontest")
    public String newcontestform() {return "newcontest";}

    @GetMapping("/user_resume")
    public String user_resumeform() {return "user_resume";}

    @GetMapping("/user_resume2")
    public String user_resume2form() {return "user_resume2";}

    @GetMapping("/resume_list")
    public String resume_listform() {return "resume_list";}

    @GetMapping("/resume_detail")
    public String resume_detailform() {return "resume_detail";}
}
