package com.hexplatoon.syncrift_backend.dto.battle.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypingConfig implements Config {
    private String text;
    private Integer duration;
}
