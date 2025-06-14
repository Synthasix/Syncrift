package com.hexplatoon.syncrift_backend.dto.battle.config;

import lombok.Builder;
import lombok.Data;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class CssConfig implements Config {
    private String imageUrl;
    private Integer duration;
    private String color1;
    private String color2;
}
