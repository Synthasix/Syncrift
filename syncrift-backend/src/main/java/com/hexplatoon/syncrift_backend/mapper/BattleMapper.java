package com.hexplatoon.syncrift_backend.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexplatoon.syncrift_backend.dto.battle.BattleHistoryDto;
import com.hexplatoon.syncrift_backend.dto.battle.Result;
import com.hexplatoon.syncrift_backend.dto.battle.config.CodeforcesConfig;
import com.hexplatoon.syncrift_backend.dto.battle.config.Config;
import com.hexplatoon.syncrift_backend.dto.battle.config.CssConfig;
import com.hexplatoon.syncrift_backend.dto.battle.config.TypingConfig;
import com.hexplatoon.syncrift_backend.entity.Battle;

// TODO : Move this mapper to global mapper
public class BattleMapper {

    public static BattleHistoryDto toBattleHistoryDto(Battle battle) {
        if (battle == null) return null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            Config obj = null;
            if (battle.getCategory() == Battle.Category.TB) obj = mapper.readValue(battle.getConfigJson(), TypingConfig.class);
            else if (battle.getCategory() == Battle.Category.CSS) obj = mapper.readValue(battle.getConfigJson(), CssConfig.class);
            else if (battle.getCategory() == Battle.Category.CF) obj = mapper.readValue(battle.getConfigJson(), CodeforcesConfig.class);

            assert obj instanceof TypingConfig;
            return BattleHistoryDto.builder()
                    .id(battle.getId())
                    .category(battle.getCategory().name())
                    .challenger(ProfileMapper.toMiniProfileDto(battle.getChallenger()))
                    .opponent(ProfileMapper.toMiniProfileDto(battle.getOpponent()))
                    .status(battle.getStatus().name())
                    .createdAt(battle.getCreatedAt())
                    .startedAt(battle.getStartedAt())
                    .updatedAt(battle.getUpdatedAt())
                    .resultJson(mapper.readValue(battle.getResultJson(), Result.class))
                    .configJson(obj)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
