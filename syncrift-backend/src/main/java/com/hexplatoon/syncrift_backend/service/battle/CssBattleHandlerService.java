package com.hexplatoon.syncrift_backend.service.battle;
import com.hexplatoon.syncrift_backend.dto.battle.Result;
import com.hexplatoon.syncrift_backend.dto.battle.config.Config;
import com.hexplatoon.syncrift_backend.dto.battle.config.CssConfig;
import com.hexplatoon.syncrift_backend.entity.Image;
import com.hexplatoon.syncrift_backend.repository.ImageRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class CssBattleHandlerService {

    private ImageRepository imageRepository;
    private BattleService battleService;
    CssBattleHandlerService(@Lazy BattleService battleService, ImageRepository imageRepository) {
        this.battleService = battleService;
        this.imageRepository = imageRepository;
    }
    private final Map<Long, Config> configMap = new ConcurrentHashMap<>();
    // create config for css battle
    public Config getConfig(Long battleId) {
        Image image = imageRepository.findRandomImage();
        Integer duration = battleService.getActiveBattleById(battleId).getDuration();
        Config config = CssConfig.builder()
                .imageUrl(image.getCloudinaryUrl())
                .duration(duration)
                .color1(image.getColor1())
                .color2(image.getColo2())
                .build();
        configMap.put(battleId, config);
        return config;
    }

    public Result getResult(Long battleId) throws IOException {
        return null;
    }

    public void saveUserText(Long battleId, String username, String text) {
    }
}
