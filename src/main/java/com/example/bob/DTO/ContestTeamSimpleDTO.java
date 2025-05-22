package com.example.bob.DTO;

import com.example.bob.Entity.ContestTeamEntity;

public record ContestTeamSimpleDTO(Long id, String teamName) {
    public static ContestTeamSimpleDTO from(ContestTeamEntity entity) {
        return new ContestTeamSimpleDTO(entity.getId(), entity.getTeamName());
    }
}
