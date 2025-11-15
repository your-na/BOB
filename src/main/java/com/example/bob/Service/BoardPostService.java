package com.example.bob.Service;

import com.example.bob.DTO.BoardPostRequestDto;
import com.example.bob.Entity.BoardCategory;
import com.example.bob.Entity.BoardPost;
import com.example.bob.Repository.BoardPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;

    // 게시글 저장 메서드
    public void savePost(BoardPostRequestDto dto) throws IOException {
        String filePath = null;

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {

            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID() + "_" + originalFileName;

            // ✅ 절대 경로로 수정
            String uploadDir = System.getProperty("user.dir") + "/uploads/boardFiles/";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("업로드 폴더 생성 실패: " + dir.getAbsolutePath());
            }

            // 최종 파일 경로
            filePath = uploadDir + savedFileName;

            try {
                file.transferTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("파일 저장 실패", e);
            }
        }

        // 엔티티 변환 및 저장
        BoardPost post = BoardPost.builder()
                .category(BoardCategory.valueOf(dto.getCategory()))
                .title(dto.getTitle())
                .content(dto.getContent())
                .writer(dto.getWriter())
                .filePath(filePath) // null이면 그냥 안 넣김
                .build();

        boardPostRepository.save(post);
    }

}
