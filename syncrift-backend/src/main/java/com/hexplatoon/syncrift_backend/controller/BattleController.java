package com.hexplatoon.syncrift_backend.controller;

import com.hexplatoon.syncrift_backend.dto.battle.BattleHistoryDto;
import com.hexplatoon.syncrift_backend.entity.Battle;
import com.hexplatoon.syncrift_backend.mapper.BattleMapper;
import com.hexplatoon.syncrift_backend.repository.BattleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
public class BattleController {

    private final BattleRepository battleRepository;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BattleHistoryDto getBattleHistory(@PathVariable Long id) {
        Optional<Battle> battle = battleRepository.findById(id);
        return battle.map(BattleMapper::toBattleHistoryDto).orElse(null);
    }
}
