package com.hexplatoon.syncrift_backend.dto.battle.config;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class CssConfig implements Config {
    private String imageUrl;
    private Integer duration;
    private List<String> colorCode;
}
