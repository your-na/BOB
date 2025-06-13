package com.example.bob.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatFileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadChatFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam("roomId") String roomId,
                                            @RequestParam("type") String type) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "빈 파일입니다."));
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path folderPath = Paths.get("uploads/chat");
            Files.createDirectories(folderPath); // 폴더 없으면 생성
            Path savePath = folderPath.resolve(fileName);

            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/chat/" + fileName;

            return ResponseEntity.ok(Map.of(
                    "fileUrl", fileUrl,
                    "fileName", file.getOriginalFilename()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "업로드 실패", "detail", e.getMessage()));
        }
    }

}
