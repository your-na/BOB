package com.example.bob.Controller;

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.Service.CoResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coresumes")
public class CoResumeEditController {

    private final CoResumeService coResumeService;
    private static final Logger logger = LoggerFactory.getLogger(CoResumeEditController.class);  // 로그 출력을 위한 로거 설정

    @Autowired
    public CoResumeEditController(CoResumeService coResumeService) {
        this.coResumeService = coResumeService;
    }

    // 수정할 이력서 데이터를 불러오는 엔드포인트 (GET) - 수정 페이지로 이동
    @GetMapping("/edit/{id}")
    public String getResumeForEdit(@PathVariable Long id, Model model) {
        logger.info("이력서 수정 요청 - ID: {}", id);  // 로그 출력

        try {
            CoResumeRequestDTO resumeDTO = coResumeService.getResumeById(id);
            logger.info("이력서 데이터 불러오기 성공 - ID: {}", id);  // 성공 로그

            // 데이터를 모델에 추가하여 HTML에 전달
            model.addAttribute("resume", resumeDTO);

            return "co_resume_edit";  // co_resume_edit.html 템플릿을 반환 (템플릿 렌더링)
        } catch (Exception e) {
            logger.error("이력서 데이터 불러오기 실패 - ID: {}", id, e);  // 실패 로그
            return "error_page";  // 실패 시 에러 페이지로 이동
        }
    }

    // 수정된 이력서 저장 처리 (POST)
    @PostMapping("/{id}/edit")
    public String updateResume(@PathVariable Long id, @ModelAttribute CoResumeRequestDTO updatedResume) {
        logger.info("이력서 수정 요청 처리 - ID: {}", id);

        try {
            coResumeService.updateResume(id, updatedResume);
            logger.info("이력서 수정 완료 - ID: {}", id);  // 수정 완료 로그
            return "redirect:/coresumelist";  // 수정 완료 후 이력서 목록 페이지로 리디렉션
        } catch (Exception e) {
            logger.error("이력서 수정 실패 - ID: {}", id, e);  // 수정 실패 로그
            return "error_page";  // 실패 시 에러 페이지로 이동
        }
    }

}
