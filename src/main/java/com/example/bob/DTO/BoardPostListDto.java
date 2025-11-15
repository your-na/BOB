package com.example.bob.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardPostListDto {
    private Long id;
    private String category;
    private String title;
    private String writer;
    private String createdAt;
}
