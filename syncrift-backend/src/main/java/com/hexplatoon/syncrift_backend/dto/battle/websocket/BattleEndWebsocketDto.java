package com.hexplatoon.syncrift_backend.dto.battle.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BattleEndWebsocketDto {
    private Long battleId;
    private String text;
}
