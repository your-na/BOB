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
import com.example.bob.Entity.BoardPostLike;
import com.example.bob.Repository.BoardPostLikeRepository;
import com.example.bob.Entity.BoardComment;
import com.example.bob.Repository.BoardCommentRepository;
import com.example.bob.Repository.BoardCommentLikeRepository;



import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.transaction.annotation.Transactional;





import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardCommentLikeRepository boardCommentLikeRepository;



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
    public BoardPostDetailDto getPostById(Long id, String username) {

        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // ❤️ 내가 좋아요 눌렀는지
        boolean likedByMe = boardPostLikeRepository
                .existsByBoardPostIdAndUsername(id, username);

        return new BoardPostDetailDto(
                post.getId(),
                post.getCategory().name(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                post.getFilePath(),
                post.getLikeCount(),
                likedByMe
        );
    }


    // ===== 좋아요 토글 메서드 =====
    @Transactional  // <- 이거 꼭 추가하세요!
    public int toggleLike(Long postId, String username) {

        // 게시글 조회
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 이미 좋아요 눌렀는지 여부 확인
        boolean alreadyLiked = boardPostLikeRepository
                .existsByBoardPostIdAndUsername(postId, username);

        if (alreadyLiked) {
            // ❤️ 좋아요 취소
            boardPostLikeRepository.deleteByBoardPostIdAndUsername(postId, username);
        } else {
            // ❤️ 좋아요 추가
            BoardPostLike like = BoardPostLike.builder()
                    .boardPost(post)
                    .username(username)
                    .build();

            boardPostLikeRepository.save(like);
        }

        // 최신 좋아요 개수 다시 계산
        int count = (int) boardPostLikeRepository.countByBoardPostId(postId);

        // 게시글 엔티티에 좋아요 개수 반영
        post.setLikeCount(count);
        boardPostRepository.save(post);

        // 프론트에 리턴될 최종 좋아요 수
        return count;
    }

    // 📌 내가 쓴 게시글 목록 조회 서비스
    public List<BoardPost> getMyPosts(String userNick) {
        return boardPostRepository.findByWriterOrderByCreatedAtDesc(userNick);
    }


    // 삭제
    @Transactional
    public void deleteMyPosts(List<Long> ids, String loginId) {
        // 1. 사용자 정보 조회
        UserEntity user = userRepository.findByUserIdLogin(loginId)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다."));
        String userNick = user.getUserNick();

        // 2. 본인 게시글만 필터링
        List<BoardPost> postsToDelete = boardPostRepository.findAllByIdInAndWriter(ids, userNick);
        if (postsToDelete.size() != ids.size()) {
            throw new RuntimeException("본인이 작성하지 않은 게시글이 포함되어 있습니다.");
        }

        // 3. 댓글 ID 수집
        List<BoardComment> comments = boardCommentRepository.findByBoardPostIdIn(ids);
        List<Long> commentIds = comments.stream()
                .map(BoardComment::getId)
                .toList();

        // 4. 삭제 순서대로 진행 (댓글하트 → 댓글 → 게시글하트 → 게시글)
        boardCommentLikeRepository.deleteByCommentIdIn(commentIds);    // 댓글 좋아요 삭제
        boardCommentRepository.deleteByBoardPostIdIn(ids);             // 댓글 삭제
        boardPostLikeRepository.deleteByBoardPostIdIn(ids);            // 게시글 좋아요 삭제
        boardPostRepository.deleteAll(postsToDelete);                  // 게시글 삭제
    }







}
