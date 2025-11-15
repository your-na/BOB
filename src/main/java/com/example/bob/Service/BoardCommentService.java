package com.example.bob.Service;

import com.example.bob.DTO.BoardCommentRequestDto;
import com.example.bob.DTO.BoardCommentResponseDto;
import com.example.bob.Entity.BoardComment;
import com.example.bob.Entity.BoardPost;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.BoardCommentRepository;
import com.example.bob.Repository.BoardPostRepository;
import com.example.bob.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.bob.Repository.BoardCommentLikeRepository;
import com.example.bob.Entity.BoardCommentLike;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository commentRepository;
    private final BoardPostRepository postRepository;
    private final UserRepository userRepository;
    private final BoardCommentLikeRepository likeRepository;

    // 댓글 저장
    public void saveComment(BoardCommentRequestDto dto) {

        // 로그인 사용자 정보 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId;

        if (principal instanceof UserDetails userDetails) {
            loginId = userDetails.getUsername();
        } else {
            throw new RuntimeException("로그인 사용자만 댓글을 작성할 수 있습니다.");
        }

        // 사용자 정보 찾기
        UserEntity user = userRepository.findByUserIdLogin(loginId)
                .orElseThrow(() -> new RuntimeException("사용자 정보 없음"));

        // 게시글 찾기
        BoardPost post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("해당 게시글이 존재하지 않습니다."));

        // 댓글 저장
        BoardComment comment = BoardComment.builder()
                .content(dto.getContent())
                .writer(user.getUserNick())   // 닉네임 저장
                .boardPost(post)
                .build();

        commentRepository.save(comment);
    }

    // 댓글 목록 조회 - DTO 형태로 반환
    public List<BoardCommentResponseDto> getCommentsByPostId(Long postId) {

        BoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("해당 게시글이 존재하지 않습니다."));

        // 🔥 로그인 사용자 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId = null;

        if (principal instanceof UserDetails userDetails) {
            loginId = userDetails.getUsername();
        }

        final UserEntity currentUser = (loginId != null)
                ? userRepository.findByUserIdLogin(loginId).orElse(null)
                : null;

        return commentRepository.findByBoardPostOrderByCreatedAtAsc(post).stream()
                .map(c -> {

                    // 🔥 좋아요 여부 확인
                    boolean liked = false;
                    if (currentUser != null) {
                        liked = likeRepository.findByUserAndComment(currentUser, c).isPresent();
                    }

                    return new BoardCommentResponseDto(
                            c.getId(),
                            c.getWriter(),
                            c.getContent(),
                            c.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
                            liked
                    );
                })
                .collect(Collectors.toList());
    }



    // 댓글 좋아요 토글
    public int toggleLike(Long commentId) {
        // 로그인 사용자 정보 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId;

        if (principal instanceof UserDetails userDetails) {
            loginId = userDetails.getUsername();
        } else {
            throw new RuntimeException("로그인 사용자만 좋아요를 누를 수 있습니다.");
        }

        // 사용자 찾기
        UserEntity user = userRepository.findByUserIdLogin(loginId)
                .orElseThrow(() -> new RuntimeException("사용자 정보 없음"));

        // 댓글 찾기
        BoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("해당 댓글이 존재하지 않습니다."));

        // 좋아요가 이미 있는지 확인
        return likeRepository.findByUserAndComment(user, comment)
                .map(existingLike -> {
                    likeRepository.delete(existingLike); // 좋아요 취소
                    return likeRepository.countByComment(comment); // 현재 좋아요 수 반환
                })
                .orElseGet(() -> {
                    likeRepository.save(BoardCommentLike.builder()
                            .user(user)
                            .comment(comment)
                            .build()); // 좋아요 추가
                    return likeRepository.countByComment(comment); // 현재 좋아요 수 반환
                });
    }
}
