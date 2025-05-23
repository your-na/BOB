package com.example.bob.DTO;

import com.example.bob.Entity.ContestTeamEntity;

public record ContestTeamSimpleDTO(Long id, String teamName, String contestTitle) {
    public static ContestTeamSimpleDTO from(ContestTeamEntity entity) {
        return new ContestTeamSimpleDTO(
                entity.getId(),
                entity.getTeamName(),
                entity.getContest().getTitle()  // ✅ 공모전 제목 추가
        );
    }
}

