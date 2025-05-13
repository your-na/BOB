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

    //기업 구인글 작성
    @GetMapping("/cojobwrite")
    public String jobwriteform() {return "co_jobwrite";}


    //기업 구인 공고 목록
    @GetMapping("/job2")
    public String jobbform() {return "job2";}

    @GetMapping("/copostcon")
    public String coconform() {return "co_postcontest";}

    //관리자 첫화면?
    @GetMapping("/placeholder")
    public String placeform() {return "ad_placeholder";}

    //기업 주최한 공모전 목록
    @GetMapping("/comycontest")
    public String myconform() {return "co_mycontest";}

    //기업 내가 쓴 공고
    @GetMapping("/comyjob")
    public String myjobform() {return "co_myjob";}

    //기업 지원자 현황 보기
    @GetMapping("/applicant")
    public String applicantform() {return "co_applicant";}

    //기업 구인 내역
    @GetMapping("/corecruit")
    public String recruitform() {return "co_recruit";}

    //프로필보기
    @GetMapping("/profileview")
    public String profileform() {return "profile_view";}

    //신청한 구직 내역
    @GetMapping("/jobapplication")
    public String jobappform() {return "job_application";}

    //기업 구인통계
    @GetMapping("/costat")
    public String costatform() {return "co_stat";}

    //공몸전 상세보기
    @GetMapping("/contest9")
    public String contest9form() {return "contest9";}

    //웹 통계(관리자)
    @GetMapping("/statistic")
    public String staticform() {return "ad_statistic";}

    //공모전 수상 팝업 알림 눌렀을때 뜨는 창(추후 수정)
    @GetMapping("/contestaward")
    public String awardform() {return "contest_award";}

    //기업 내 정보
    @GetMapping("/coprofile")
    public String profiform() {return "co_profile";}
}
