package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassRequestDTO {

    private Long jobPostId;   // 📄 공고 ID
    private Long resumeId;    // 🧾 이력서 ID (선택)
    private String message;   // 💬 전달 메시지

}
