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

    //기업 구인글 작성
    @GetMapping("/cojobwrite")
    public String jobwriteform() {return "co_jobwrite";}

    //기업 구인글 공고 상세
    @GetMapping("/jobdetail")
    public String jobdetailform() {return "co_jobdetail";}

    //기업 구인 공고 목록
    @GetMapping("/job2")
    public String jobbform() {return "job2";}

    //프로젝트쪽 제출한 신청서 보기 버튼 눌렀을 때
    @GetMapping("/projapplication2")
    public String appform() {return "projapplication2";}

    @GetMapping("/copostcon")
    public String coconform() {return "co_postcontest";}

    //관리자 첫화면?
    @GetMapping("/placeholder")
    public String placeform() {return "ad_placeholder";}

    //관리자 기업 공모전 목록
    @GetMapping("/adcomcont")
    public String comconform() {return "ad_comcontest";}

    //외부
    @GetMapping("/adnotcont")
    public String notconform() {return "ad_notcomcontest";}
}
