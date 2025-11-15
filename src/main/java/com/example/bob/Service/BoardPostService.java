package com.example.bob.Service;

import com.example.bob.DTO.BoardPostRequestDto;
import com.example.bob.Entity.BoardCategory;
import com.example.bob.Entity.BoardPost;
import com.example.bob.Repository.BoardPostRepository;
import org.springframework.web.multipart.MultipartFile;
import com.example.bob.DTO.BoardPostListDto;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;





import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;


    // 게시글 저장 메서드
    public void savePost(BoardPostRequestDto dto) throws IOException {

        // ===============================
        // 1) 첨부파일 저장
        // ===============================
        String filePath = null;

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {

            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID() + "_" + originalFileName;

            // 절대 경로
            String uploadDir = System.getProperty("user.dir") + "/uploads/boardFiles/";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("업로드 폴더 생성 실패: " + dir.getAbsolutePath());
            }

            filePath = uploadDir + savedFileName;

            try {
                file.transferTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("파일 저장 실패", e);
            }
        }


        // ===============================
        // 2) 로그인된 사용자 닉네임 가져오기
        // ===============================
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userNick;

        if (principal instanceof UserDetails userDetails) {
            // 로그인 아이디 = userIdLogin
            String loginId = userDetails.getUsername();

            UserEntity user = userRepository.findByUserIdLogin(loginId)
                    .orElseThrow(() -> new RuntimeException("사용자 정보가 존재하지 않습니다."));

            userNick = user.getUserNick(); // ★ DB에 저장할 닉네임
        } else {
            throw new RuntimeException("로그인 정보가 없습니다.");
        }


        // ===============================
        // 3) Entity 변환 + DB 저장
        // ===============================
        BoardPost post = BoardPost.builder()
                .category(BoardCategory.valueOf(dto.getCategory()))
                .title(dto.getTitle())
                .content(dto.getContent())
                .writer(userNick)  // ★ 로그인한 사용자의 닉네임 저장
                .filePath(filePath)
                .build();

        boardPostRepository.save(post);
    }


    //게시글 목록
    public List<BoardPostListDto> getAllPosts() {
        return boardPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> new BoardPostListDto(
                        post.getId(),
                        post.getCategory().name(),   // 혹은 post.getCategory().getDisplayName()
                        post.getTitle(),
                        post.getWriter(),
                        post.getCreatedAt().format(DateTimeFormatter.ofPattern("yy/MM/dd HH:mm"))
                ))
                .collect(Collectors.toList());
    }


}
