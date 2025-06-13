package com.hexplatoon.syncrift_backend.dto.battle.websocket;

import com.hexplatoon.rivalist_backend.dto.battle.config.Config;
import com.hexplatoon.rivalist_backend.entity.Battle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleStartWebsocketDto {
    private Long battleId;
    private Config config;
    private Battle.Category category;
}
