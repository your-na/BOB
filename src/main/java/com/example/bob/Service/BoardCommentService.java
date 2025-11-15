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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository commentRepository;
    private final BoardPostRepository postRepository;
    private final UserRepository userRepository;

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

        return commentRepository.findByBoardPostOrderByCreatedAtAsc(post).stream()
                .map(c -> new BoardCommentResponseDto(
                        c.getWriter(),                                      // 작성자 닉네임
                        c.getContent(),                                     // 댓글 내용
                        c.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")) // 날짜 변환
                ))
                .collect(Collectors.toList());
    }
}
