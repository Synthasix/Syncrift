
package com.hexplatoon.syncrift_backend.controller;

import com.hexplatoon.syncrift_backend.dto.battle.websocket.BattleEndWebsocketDto;
import com.hexplatoon.syncrift_backend.entity.Battle;
import com.hexplatoon.syncrift_backend.service.battle.BattleService;
import com.hexplatoon.syncrift_backend.service.battle.CssBattleHandlerService;
import com.hexplatoon.syncrift_backend.service.battle.TypingBattleHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BattleWebSocketController {

    private final BattleService battleService;
    private final TypingBattleHandlerService typingBattleHandlerService;
    private final CssBattleHandlerService cssBattleHandlerService;

    @MessageMapping({"/battle/ready"})
    public void handleReadiness(@Payload Long battleId, Principal principal) {
        String username = (String) principal.getName();
        battleService.updateReadiness(username, battleId);
    }

    @MessageMapping({"/battle/end"})
    public void calculateScore(@Payload BattleEndWebsocketDto dto, Principal principal) {
        String username = (String) principal.getName();
        Battle battleType = battleService.getActiveBattleById(dto.getBattleId());
        if(battleType.getCategory() == Battle.Category.TB){
            typingBattleHandlerService.saveUserText(dto.getBattleId(), username, dto.getText());
        }else if(battleType.getCategory() == Battle.Category.CSS){
//            System.out.println(dto.getText());
            cssBattleHandlerService.saveUserText(dto.getBattleId(), username, dto.getText());
        }
    }
}
