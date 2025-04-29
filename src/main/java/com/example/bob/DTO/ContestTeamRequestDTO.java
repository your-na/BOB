package com.example.bob.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContestTeamRequestDTO {
    private Long contestId;
    private List<Long> memberIds;  // 비어 있으면 혼자 참가
}
