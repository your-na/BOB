package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ComContestController {
    @GetMapping("/comhome")
    public String contest() {return "comhome";}
}
