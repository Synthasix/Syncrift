package com.hexplatoon.syncrift_backend.dto.battle.websocket;


import com.hexplatoon.rivalist_backend.dto.battle.Result;
import com.hexplatoon.rivalist_backend.entity.Battle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BattleResultWebsocketDto {
    private Long battleId;
    private Result result;
    private Battle.Category category;
}
