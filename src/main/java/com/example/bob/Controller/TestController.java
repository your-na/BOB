package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    //기업측 이력서 양식 제작
    @GetMapping("/coresume")
    public String newcontestform() {return "co_resume";}

    //기업측 이력서 목록
    @GetMapping("/coresumelist")
    public String resumelistform() {return "co_resumelist";}

    //구직 공고 상세
    @GetMapping("/jobindex")
    public String jobinform() {return "jobindex";}
}
