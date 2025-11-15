package com.example.bob.Service;

import com.example.bob.DTO.BoardPostRequestDto;
import com.example.bob.Entity.BoardCategory;
import com.example.bob.Entity.BoardPost;
import com.example.bob.Repository.BoardPostRepository;
import org.springframework.web.multipart.MultipartFile;
import com.example.bob.DTO.BoardPostListDto;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.DTO.BoardPostDetailDto;
import java.time.format.DateTimeFormatter;



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
        String fileWebPath = null; // ★ 웹에서 접근할 경로만 저장!

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {

            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID() + "_" + originalFileName;

            // 실제 저장 경로 (절대 경로)
            String uploadDir = System.getProperty("user.dir") + "/uploads/boardFiles/";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("업로드 폴더 생성 실패: " + dir.getAbsolutePath());
            }

            // OS 절대 경로로 파일 저장
            String fullPath = uploadDir + savedFileName;
            try {
                file.transferTo(new File(fullPath));
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("파일 저장 실패", e);
            }

            // ★ 웹에서 접근 가능한 경로만 저장!
            fileWebPath = "/uploads/boardFiles/" + savedFileName;
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
                .filePath(fileWebPath) // ★ 웹 경로 저장!
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

    // 게시글 상세보기 조회
    public BoardPostDetailDto getPostById(Long id) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        return new BoardPostDetailDto(
                post.getId(),
                post.getCategory().name(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                post.getFilePath()
        );
    }



}
