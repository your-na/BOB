package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class CoJobViewController {

    @GetMapping("/cojobdetail")
    public String goToCojobDetailPage() {
        return "co_jobdetail"; // templates/cojobdetail.html 또는 static/cojobdetail.html
    }
}
