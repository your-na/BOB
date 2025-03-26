package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    //기업측 이력서 양식 제작
    @GetMapping("/coresume")
    public String newcontestform() {return "co_resume";}
}
