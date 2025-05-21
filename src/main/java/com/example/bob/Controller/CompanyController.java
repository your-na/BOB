package com.example.bob.Controller;

import com.example.bob.DTO.CompanyDTO;
import com.example.bob.Repository.CompanyRepository;
import com.example.bob.Service.CompanyService;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import com.example.bob.Service.DashboardService;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final DashboardService dashboardService;


    @GetMapping("/co_signup")
    public String signupPage() {
        return "co_signup";
    }

    @PostMapping("/co_signup")
    public String joinCo(@RequestParam String phone1,
                         @RequestParam String phone2,
                         @RequestParam String phone3,
                         @ModelAttribute CompanyDTO companyDTO) {
        // 전화번호 연결
        String coPhone = phone1 + "-" + phone2 + "-" + phone3;
        companyDTO.setCoPhone(coPhone);

        try {
            companyService.save(companyDTO);
        }catch (Exception e){
            e.printStackTrace();
            return "co_signup";
        }
        return "redirect:/login";
    }

}
